package akapps.photoediting.base.model

data class Resource<out T>(
        val state: ResourceState,
        val data: T? = null,
        val message: String? = null
)