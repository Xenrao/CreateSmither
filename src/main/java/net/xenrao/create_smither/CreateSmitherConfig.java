package net.xenrao.create_smither;

import net.neoforged.neoforge.common.ModConfigSpec;

public class CreateSmitherConfig {

    private static final double DEFAULT_SMITHER_IMPACT = 8.0;

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    static {
        BUILDER.push("kinetics");
        BUILDER.push("stress_values");
        BUILDER.push("impact");
    }

    public static final ModConfigSpec.DoubleValue MECHANICAL_SMITHER_IMPACT = BUILDER
            .comment("Stress impact of the Mechanical Smither at 1 RPM (in SU)")
            .defineInRange("mechanicalSmitherImpact", DEFAULT_SMITHER_IMPACT, 0.0, 1024.0);

    static {
        BUILDER.pop();
        BUILDER.pop();
        BUILDER.pop();
    }

    public static final ModConfigSpec SPEC = BUILDER.build();

    public static double getSmitherImpact() {
        return SPEC.isLoaded() ? MECHANICAL_SMITHER_IMPACT.get() : DEFAULT_SMITHER_IMPACT;
    }
}