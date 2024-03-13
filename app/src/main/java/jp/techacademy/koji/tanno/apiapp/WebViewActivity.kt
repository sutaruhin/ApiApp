package jp.techacademy.koji.tanno.apiapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import jp.techacademy.koji.tanno.apiapp.FavoriteShop.Companion.findBy
import kotlinx.android.synthetic.main.activity_web_view.*
import java.io.Serializable

class WebViewActivity : AppCompatActivity() {

    var isFavorite: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)

        // ActionBarの追加
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "クーポン詳細"

        //Web読み込み
        webView.loadUrl(intent.getStringExtra(KEY_URL).toString())

        // Shop情報を受取
        val id = intent.getStringExtra(KEY_ID).toString()
        val name = intent.getStringExtra(KEY_NAME).toString()
        val address = intent.getStringExtra(KEY_ADDRESS).toString()
        val imageUrl = intent.getStringExtra(KEY_IMAGEURL).toString()

        // お気に入り状態を取得
        isFavorite = FavoriteShop.findBy(id) != null

        // お気に入りマークを表示し、リスナーセット
        webFavoriteImageView.apply {
            setImageResource(if (isFavorite) R.drawable.ic_star else R.drawable.ic_star_border) // Picassoというライブラリを使ってImageViewに画像をはめ込む
            setOnClickListener {
                if (isFavorite) {
                    showConfirmDeleteFavoriteDialog(id)
                } else {
                    FavoriteShop.insert(FavoriteShop().apply {
                        this.id = id
                        this.name = name
                        this.address = address
                        this.imageUrl = imageUrl
                        this.url = url
                    })
                    setImageResource(R.drawable.ic_star)
                    isFavorite=true
                    //MainActivity().updateFragments()
                }

            }
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item?.itemId) {
            android.R.id.home -> finish()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun showConfirmDeleteFavoriteDialog(id: String) {
        AlertDialog.Builder(this)
            .setTitle(R.string.delete_favorite_dialog_title)
            .setMessage(R.string.delete_favorite_dialog_message)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                FavoriteShop.delete(id)
                webFavoriteImageView.setImageResource(R.drawable.ic_star_border)
                isFavorite=false
            }  // ???
            .setNegativeButton(android.R.string.cancel) { _, _ -> }
            .create()
            .show()
    }

    companion object {
        private const val KEY_URL = "key_url"
        private const val KEY_ID = "key_id"
        private const val KEY_NAME = "key_name"
        private const val KEY_ADDRESS = "key_address"
        private const val KEY_IMAGEURL = "key_imageUrl"
        fun start(activity: Activity, url: String, id: String, name: String, address: String, imageUrl: String) {
            val intent = Intent(activity, WebViewActivity::class.java)
            intent.putExtra(KEY_URL, url)
            intent.putExtra(KEY_ID, id)
            intent.putExtra(KEY_NAME, name)
            intent.putExtra(KEY_ADDRESS, address)
            intent.putExtra(KEY_IMAGEURL, imageUrl)
            activity.startActivity(intent)

        }
    }

}
