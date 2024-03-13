package jp.techacademy.koji.tanno.apiapp

import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import io.realm.kotlin.ext.query

class FavoriteShop: RealmObject {
    @PrimaryKey
    var id: String = ""
    var imageUrl: String = ""
    var name: String = ""
    var url: String = ""
    var address: String = ""

    companion object {
        fun findAll(): List<FavoriteShop> { // お気に入りのShopを全件取得
            val config = RealmConfiguration.Builder(schema = setOf(FavoriteShop::class)).deleteRealmIfMigrationNeeded()
                .build()
            val realm = Realm.open(config)

            return realm.query<FavoriteShop>().find().let {
                    realm.copyFromRealm(it)
                }
            }

        fun findBy(id: String): FavoriteShop? { // お気に入りされているShopをidで検索して返す。お気に入りに登録されていなければnullで返す
            val config = RealmConfiguration.Builder(schema = setOf(FavoriteShop::class)).deleteRealmIfMigrationNeeded()
                .build()
            val realm = Realm.open(config)

            return realm.query<FavoriteShop>("id == $0", id).first().find()?.let {
                realm.copyFromRealm(it)
            }
        }

        fun insert(favoriteShop: FavoriteShop) { // お気に入り追加
            val config = RealmConfiguration.Builder(schema = setOf(FavoriteShop::class)).deleteRealmIfMigrationNeeded()
                .build()
            val realm = Realm.open(config)
            realm.writeBlocking {
                val upsertShop: FavoriteShop? = this.query<FavoriteShop>("id == $0", favoriteShop.id).first().find()
                if (upsertShop != null) {
                    upsertShop.name = favoriteShop.name
                    upsertShop.imageUrl = favoriteShop.imageUrl
                    upsertShop.url = favoriteShop.url
                    upsertShop.address = favoriteShop.address
                } else {
                    copyToRealm(favoriteShop)
                }
            }
        }

        fun delete(id:String) { // idでお気に入りから削除する
            val config = RealmConfiguration.Builder(schema = setOf(FavoriteShop::class)).deleteRealmIfMigrationNeeded()
                .build()
            val realm = Realm.open(config)
            realm.writeBlocking {
                val deleteShop: FavoriteShop? = this.query<FavoriteShop>("id == $0", id).first().find()
                if (deleteShop != null) delete(deleteShop)
            }
        }


    }
}