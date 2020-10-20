package com.lordmau5.ffs.util;

import com.electronwill.nightconfig.core.EnumGetMethod;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.config.ModConfig;

import javax.annotation.Nullable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class Config {

    /* Walker */

    static class ConfigBinding {
        final ITypeAdapter adapter;
        final Field field;
        final ForgeConfigSpec.ConfigValue value;

        @Nullable
        final Object obj;

        ConfigBinding(final ITypeAdapter adapter, final Field field, @Nullable final Object obj, final ForgeConfigSpec.ConfigValue value) {
            this.adapter = adapter;
            this.field = field;
            this.obj = obj;
            this.value = value;
        }
    }

    public static ForgeConfigSpec walkClass(final Class<?> cls, final IEventBus bus) {
        return new ConfigWalker(cls, bus).getSpec();
    }

    public static class ConfigWalker {
        private final Class<?> cls;
        @Nullable
        private Set<Class<?>> classes;

        private boolean built = false;
        @Nullable
        private Set<ConfigBinding> bindings = null;
        @Nullable
        private ForgeConfigSpec spec = null;

        public ConfigWalker(final Class<?> cls) {
            this.cls = cls;
        }

        public ConfigWalker(final Class<?> cls, final IEventBus bus) {
            this(cls);
            listen(bus);
        }

        public ConfigWalker listen(final IEventBus bus) {
            bus.addListener(this::onConfigEvent);
            return this;
        }

        private void onConfigEvent(final ModConfig.ModConfigEvent event) {
            final ModConfig config = event.getConfig();
            if ( built && config.getSpec() == spec )
                update();
        }

        public ConfigWalker update() {
            if ( !built )
                build();

//            WirelessUtils.LOG.info("Updating config.");

            if ( bindings == null || bindings.isEmpty() )
                return this;

            for (final ConfigBinding binding : bindings)
                binding.adapter.setValue(binding.obj, binding.field, binding.value.get());

            return this;
        }

        public ForgeConfigSpec getSpec() {
            if ( !built )
                build();

            return spec;
        }

        public void build() {
            if ( built )
                return;

            built = true;
            bindings = Sets.newHashSet();
            classes = Sets.newHashSet();

            discoverClasses(cls, 0);

            final ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
            scan(null, builder, 0);
            spec = builder.build();
        }

        private void discoverClasses(final Class<?> cls, final int depth) {
            for (final Class<?> c : cls.getClasses()) {
                if ( c == null || this.cls.equals(c) || classes.contains(c) )
                    continue;

                classes.add(c);
                if ( depth < 50 )
                    discoverClasses(c, depth + 1);
            }
        }

        private void scan(@Nullable final Object obj, final ForgeConfigSpec.Builder builder, final int depth) {
            final Class<?> cls;
            if ( obj == null )
                cls = this.cls;
            else
                cls = obj.getClass();

            for (final Field field : cls.getFields()) {
                if ( field.isAnnotationPresent(Ignore.class) )
                    continue;

                final int mods = field.getModifiers();
                if ( Modifier.isStatic(mods) && obj != null ) {
//                    WirelessUtils.LOG.warn("Skipping static field in non-root config: " + field.getName());
                    continue;
                }

                final Class<?> type = field.getType();
                final ITypeAdapter adapter = type.isEnum() ? ADAPTERS.get(Enum.class) : ADAPTERS.get(type);
                boolean is_class = false;
                if ( adapter == null && !ADAPTERS.containsKey(type) && classes.contains(type) )
                    is_class = true;
                else if ( adapter == null ) {
//                    WirelessUtils.LOG.warn("Skipping field with un-handleable type: " + field.getName() + " (Type: " + field.getType() + ")");
                    continue;
                } else if ( !adapter.canHandle(obj, field) )
                    continue;

                String name = field.getName();
                final Name annoName = field.getAnnotation(Name.class);
                if ( annoName != null )
                    name = annoName.value();

                final Translation annoTL = field.getAnnotation(Translation.class);
                if ( annoTL != null )
                    builder.translation(annoTL.value());

                final Comment annoComment = field.getAnnotation(Comment.class);
                if ( annoComment != null )
                    builder.comment(annoComment.value());

                if ( field.isAnnotationPresent(RequiresWorldRestart.class) )
                    builder.worldRestart();

                if ( is_class ) {
                    builder.push(name);

                    if ( depth >= 50 ) {
//                        WirelessUtils.LOG.warn("Config is nested too deeply. Skipping field: " + name);
                    }
                    else {
                        Object value = null;
                        boolean valid = true;

                        try {
                            value = field.get(obj);
                        } catch (final IllegalAccessException ex) {
//                            WirelessUtils.LOG.error("Unable to scan instance. Skipping field: " + name);
                            valid = false;
                        }

                        if ( valid )
                            scan(value, builder, depth + 1);
                    }

                    builder.pop();

                } else {
                    final ForgeConfigSpec.ConfigValue value = adapter.define(name, obj, field, builder, this);
                    Objects.requireNonNull(value);

                    bindings.add(new ConfigBinding(adapter, field, obj, value));
                }
            }
        }
    }


    /* Type Adapters */

    private final static Map<Class<?>, ITypeAdapter> ADAPTERS = Maps.newHashMap();

    public interface ITypeAdapter {
        default void setValue(@Nullable final Object obj, final Field field, final Object value) {
            try {
                field.set(obj, value);
            } catch (final IllegalArgumentException | IllegalAccessException ex) {
//                WirelessUtils.LOG.error("Unable to set value for field: " + field.getName(), ex);
            }
        }

        default boolean canHandle(@Nullable final Object obj, final Field field) {
            try {
                field.get(obj);
            } catch (final IllegalAccessException ex) {
                return false;
            }
            return true;
        }

        ForgeConfigSpec.ConfigValue define(String name, @Nullable Object obj, Field field, ForgeConfigSpec.Builder builder, ConfigWalker walker);
    }

    public static void registerAdapter(final Class<?> cls, final ITypeAdapter adapter) {
        ADAPTERS.put(cls, adapter);
    }

    public static void registerAdapter(final ITypeAdapter adapter, final Class<?>... classes) {
        for (final Class<?> cls : classes)
            ADAPTERS.put(cls, adapter);
    }

    static {
        registerAdapter(Enum.class, new ITypeAdapter() {
            @Override
            public boolean canHandle(@Nullable final Object obj, final Field field) {
                if ( !field.getType().isEnum() )
                    return false;

                try {
                    field.get(obj);
                } catch (final IllegalAccessException ex) {
                    return false;
                }
                return true;
            }

            @SuppressWarnings("unchecked")
            @Nullable
            @Override
            public ForgeConfigSpec.ConfigValue define(final String name, @Nullable final Object obj, final Field field, final ForgeConfigSpec.Builder builder, final ConfigWalker walker) {
                final Class<?> cls = field.getType();
                if ( !Enum.class.isAssignableFrom(cls) )
                    return null;

                return buildEnum(name, obj, field, builder, walker, (Class<? extends Enum>) cls);
            }

            @SuppressWarnings("unchecked")
            private <V extends Enum<V>> ForgeConfigSpec.EnumValue<V> buildEnum(final String name, @Nullable final Object obj, final Field field, final ForgeConfigSpec.Builder builder, final ConfigWalker walker, final Class<V> clazz) {
                V value = null;
                try {
                    value = (V) field.get(obj);
                } catch (final IllegalAccessException | ClassCastException ignored) {
                }

                final EnumMethod anno = field.getAnnotation(EnumMethod.class);
                if ( anno != null )
                    return builder.defineEnum(name, value, anno.value());

                return builder.defineEnum(name, value);
            }
        });

        registerAdapter((name, obj, field, builder, walker) ->

        {
            boolean value = false;
            try {
                value = field.getBoolean(obj);
            } catch (final IllegalAccessException ignored) {
            }

            return builder.define(name, value);
        }, Boolean.class, Boolean.TYPE);

        registerAdapter((name, obj, field, builder, walker) ->

        {
            int value = 0;
            try {
                value = field.getInt(obj);
            } catch (final IllegalAccessException ignored) {
            }

            final RangeInt anno = field.getAnnotation(RangeInt.class);
            if ( anno == null )
                return builder.define(name, value);

            return builder.defineInRange(name, value, anno.min(), anno.max());

        }, Integer.TYPE, Integer.class);

        registerAdapter((name, obj, field, builder, walker) ->

        {
            long value = 0;
            try {
                value = field.getLong(obj);
            } catch (final IllegalAccessException ignored) {
            }

            final RangeLong anno = field.getAnnotation(RangeLong.class);
            if ( anno == null )
                return builder.define(name, value);

            return builder.defineInRange(name, value, anno.min(), anno.max());

        }, Long.TYPE, Long.class);

        registerAdapter((name, obj, field, builder, walker) ->

        {
            double value = 0;
            try {
                value = field.getDouble(obj);
            } catch (final IllegalAccessException ignored) {
            }

            final RangeDouble anno = field.getAnnotation(RangeDouble.class);
            if ( anno == null )
                return builder.define(name, value);

            return builder.defineInRange(name, value, anno.min(), anno.max());

        }, Double.TYPE, Double.class);
    }


    /* Decorators */

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD})
    public @interface Ignore {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD})
    public @interface Name {
        String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.TYPE})
    public @interface Translation {
        String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD})
    public @interface Comment {
        String[] value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD})
    public @interface RangeInt {
        int min() default Integer.MIN_VALUE;

        int max() default Integer.MAX_VALUE;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD})
    public @interface RangeDouble {
        double min() default Double.MIN_VALUE;

        double max() default Double.MAX_VALUE;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD})
    public @interface RangeLong {
        long min() default Long.MIN_VALUE;

        long max() default Long.MAX_VALUE;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.TYPE})
    public @interface RequiresWorldRestart {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD})
    public @interface EnumMethod {
        EnumGetMethod value() default EnumGetMethod.NAME_IGNORECASE;
    }

}
