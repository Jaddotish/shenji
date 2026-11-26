package shenji.realms;

import net.fabricmc.api.ModInitializer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import shenji.realms.worldgen.RealmsChunkGenerator;

public class Realms implements ModInitializer {
    @Override
    public void onInitialize() {
        Registry.register(BuiltInRegistries.CHUNK_GENERATOR, "shenji_realms:terrain", RealmsChunkGenerator.CODEC);
    }
}
