package jp.techacademy.koji.tanno.apiapp

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_favorite.*

class FavoriteFragment: Fragment() {

    private val favoriteAdapter by lazy { FavoriteAdapter(requireContext())}

    // FavoriteFragment -> MainActivityに削除を通知する
    private var fragmentCallback : FragmentCallback? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is FragmentCallback) {    // contextがFragmentCallbackだったら ＝ MainActivityだったら
            fragmentCallback = context        // MainActivityへの参照を変数に格納しておく
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // fragment_favorite.xmlが反映されたViewを作成して、returnします
        return inflater.inflate(R.layout.fragment_favorite, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // ここから初期化処理を行う
        // FavoriteAdapterのお気に入り削除用メソッドの追加を行う
        favoriteAdapter.apply {
            // Adapterの処理をそのままActivityに通知
            onClickDeleteFavorite = {
                fragmentCallback?.onDeleteFavorite(it.id)   // MainActivityのonDeleteFavoriteメソッドを、FavoriteAdapterのonClickDeleteFavoriteに登録
            }
            // Itemをクリックしたとき
            onClickItem = {url, id, name, address, imageUrl ->
                fragmentCallback?.onClickItem(url, id, name, address, imageUrl)
            }
        }
        // RecyclerViewの初期化
        recyclerview.apply {
            adapter = favoriteAdapter
            layoutManager = LinearLayoutManager(requireContext()) // 一列ずつ表示
        }
        swipeRefreshLayout.setOnRefreshListener {
            updateData()
        }
        updateData()
    }

    fun updateData() {
        favoriteAdapter.refresh(FavoriteShop.findAll())
        swipeRefreshLayout.isRefreshing = false
    }
}