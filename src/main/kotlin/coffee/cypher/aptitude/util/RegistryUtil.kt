package coffee.cypher.aptitude.util

import coffee.cypher.aptitude.Aptitude
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import org.quiltmc.qkl.wrapper.minecraft.registry.RegistryDsl
import org.quiltmc.qkl.wrapper.minecraft.registry.RegistryObject
import org.quiltmc.qkl.wrapper.minecraft.registry.RegistryScope

fun <T : Any> register(registration: RegistryScope.() -> T): Lazy<T> {
    return lazy {
        RegistryScope(Aptitude.id).registration()
    }
}

@RegistryDsl
infix fun <T> RegistryObject<T>.toGenericRegistry(registry: Registry<in T>): T {
    return Registry.register(registry, Identifier(modid, path), t)
}
