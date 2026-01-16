package fr.lucwaw.utou.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.lucwaw.utou.data.repository.OffFirstUserRepository
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
            .forAddress("localhost", 8080)
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

    @Provides
    @Singleton
    fun provideUserRepository(
        stub: UserServiceGrpcKt.UserServiceCoroutineStub,
        pingStub: PingServiceGrpcKt.PingServiceCoroutineStub
    ): UserRepository {
        return OffFirstUserRepository(stub, pingStub)
    }
}
