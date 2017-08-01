package com.lordmau5.ffs.compat.top;

import com.google.common.base.Function;
import mcjty.theoneprobe.api.ITheOneProbe;

import javax.annotation.Nullable;


/**
 * Created by Gigabit101 on 31/07/2017.
 */
public class GetTheOneProbe implements Function<ITheOneProbe, Void>
{
    @Nullable
    @Override
    public Void apply(ITheOneProbe iTheOneProbe)
    {
        iTheOneProbe.registerProvider(new TankInfoProvider());
        return null;
    }
}
