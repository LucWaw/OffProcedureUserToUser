package fr.lucwaw.utou.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.lucwaw.utou.data.repository.GrpcUserRepository
import fr.lucwaw.utou.data.repository.UserRepository
import fr.lucwaw.utou.ping.PingServiceGrpcKt
import fr.lucwaw.utou.user.UserServiceGrpcKt
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object GrpcModule {

    @Provides
    @Singleton
    fun provideChannel(): ManagedChannel =
        ManagedChannelBuilder
            .forAddress("10.0.2.2", 50052)
            .usePlaintext()
            .build()

    @Provides
    @Singleton
    fun provideUserStub(channel: ManagedChannel): UserServiceGrpcKt.UserServiceCoroutineStub =
        UserServiceGrpcKt.UserServiceCoroutineStub(channel)

    @Provides
    @Singleton
    fun providePingStub(channel: ManagedChannel): PingServiceGrpcKt.PingServiceCoroutineStub =
        PingServiceGrpcKt.PingServiceCoroutineStub(channel)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindUserRepository(
        impl: GrpcUserRepository
    ): UserRepository
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface UserRepositoryEntryPoint {
    fun userRepository(): UserRepository
}
