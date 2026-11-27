package shenji.realms.worldgen.noise;

import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.level.levelgen.RandomState;

public class SeedContext {
    private final PositionalRandomFactory seedFactory;

    public SeedContext(RandomState randomState) {
        this.seedFactory = randomState.getOrCreateRandomFactory(Identifier.parse("shenji_realms:terrain"));
    }

    public int deriveSeed(String name) {
        RandomSource randomSource = seedFactory.fromHashOf(name);
        return randomSource.nextInt();
    }
}
