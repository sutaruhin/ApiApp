package jp.techacademy.koji.tanno.apiapp

import android.app.Application
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration

class ApiApplication: Application() {

    override fun onCreate() {

        super.onCreate()

        //val config = RealmConfiguration.Builder(schema = setOf(FavoriteShop::class)).deleteRealmIfMigrationNeeded()
        //    .build()
        //val realm = Realm.open(config)

    }

}