package fr.lucwaw.utou.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import fr.lucwaw.utou.data.AppDatabase
import fr.lucwaw.utou.data.dao.UserDao
import fr.lucwaw.utou.data.repository.OffFirstUserRepository
import fr.lucwaw.utou.data.repository.UserRepository
import fr.lucwaw.utou.data.workers.SyncScheduler
import fr.lucwaw.utou.data.workers.SyncUsersWorker
import fr.lucwaw.utou.ping.PingServiceGrpcKt
import fr.lucwaw.utou.user.UserServiceGrpcKt
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object GrpcModule {

    @Singleton
    @Provides
    fun provideChannel(): ManagedChannel =
        ManagedChannelBuilder
            .forAddress("localhost", 8080)
            .usePlaintext()
            .build()

    @Singleton
    @Provides
    fun provideUserStub(channel: ManagedChannel): UserServiceGrpcKt.UserServiceCoroutineStub =
        UserServiceGrpcKt.UserServiceCoroutineStub(channel)

    @Singleton
    @Provides
    fun providePingStub(channel: ManagedChannel): PingServiceGrpcKt.PingServiceCoroutineStub =
        PingServiceGrpcKt.PingServiceCoroutineStub(channel)

    @Singleton
    @Provides
    fun provideAppDatabase(
        @ApplicationContext app: Context
    ) = Room.databaseBuilder(
        app, AppDatabase::class.java, "AppDatabase",
    ).build()

    @Singleton
    @Provides
    fun provideUserDao(db: AppDatabase) = db.userDao()

    @Singleton
    @Provides
    fun provideSyncScheduler(@ApplicationContext app: Context): SyncScheduler {
        return SyncScheduler(app)
    }


    @Singleton
    @Provides
    fun provideUserRepository(
        stub: UserServiceGrpcKt.UserServiceCoroutineStub,
        pingStub: PingServiceGrpcKt.PingServiceCoroutineStub,
        userDao: UserDao,
        syncScheduler: SyncScheduler
    ): UserRepository {
        return OffFirstUserRepository(stub, pingStub, userDao, syncScheduler)
    }
}
