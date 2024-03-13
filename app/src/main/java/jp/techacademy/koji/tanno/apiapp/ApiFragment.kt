package jp.techacademy.koji.tanno.apiapp

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_api.*
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException

class ApiFragment: Fragment() {

        private val apiAdapter by lazy { ApiAdapter(requireContext())}
        private val handler = Handler(Looper.getMainLooper())

        private var fragmentCallback : FragmentCallback?= null // Fragment -> Activity にFavoriteの変更を通知する

        private var page = 0

        // Apiでデータを読み込み中ですフラグ。追加ページの読み込みの時にこれがないと、連続して読み込んでしまうので、それの制御のため
        private var isLoading = false

        override fun onAttach(context: Context) {
                super.onAttach(context)
                if (context is FragmentCallback) {      // contextがFragmentCallbackだったら ＝ MainActivityだったら
                        fragmentCallback = context      // MainActivityへの参照を変数に格納しておく
                }
        }

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
                return inflater.inflate(R.layout.fragment_api, container, false) // fragment_api.xmlが反映されたViewを作成して、returnします
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
                super.onViewCreated(view, savedInstanceState)
                // ここから初期化処理を行う
                // ApiAdapterのお気に入り追加、削除用のメソッドの追加を行う
                apiAdapter.apply {
                        onClickAddFavorite = {// Adapterの処理をそのままActivityに通知する
                                fragmentCallback?.onAddFavorite(it)   // MainActivityのonAddFavoriteメソッドを、ApiAdapterのonClickDeleteFavoriteに登録
                        }
                        onClickDeleteFavorite = { // Adapterの処理をそのままActivityに通知する
                                fragmentCallback?.onDeleteFavorite(it.id)   // MainActivityのonDeleteFavoriteメソッドを、ApiAdapterのonClickDeleteFavoriteに登録
                        }
                        // Itemをクリックしたとき
                        onClickItem = {url, id, name, address, imageUrl ->
                                fragmentCallback?.onClickItem(url, id, name, address, imageUrl)
                        }
                }

                // RecyclerViewの初期化
                recyclerview.apply {
                        adapter = apiAdapter
                        layoutManager = LinearLayoutManager(requireContext()) // 一列ずつ表示

                        addOnScrollListener(object: RecyclerView.OnScrollListener() {
                                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) { // dx は横方向の変化量、dyは縦方向の変化量
                                        super.onScrolled(recyclerView, dx, dy)
                                        if (dy == 0) { // 縦方向の変化量（スクロール量）が0の時は動いていないので何も処理はしない
                                                return
                                        }
                                        val totalCount = apiAdapter.itemCount // RecyclerViewの現在の表示アイテム数
                                        val lastVisibleItem = (layoutManager as LinearLayoutManager).findLastVisibleItemPosition() // RecyclerViewの現在見えている最後のViewHolderのposition
                                        // totalCountとlastVisibleItemから全体のアイテム数のうちどこまでが見えているかがわかる（例：totalCountが20, lastVisibleItemが15の時は、現在のスクロール位置から下に5件見えていないアイテムがある）
                                        // 一番下にスクロールした時に次の20件を表示する等の実装が可能になる。
                                        // ユーザビリティを考えると、一番下にスクロールしてから追加した場合、一度スクロールが止まるので、ユーザーは気づきにくい
                                        // ここでは、一番下から5番目を表示したときに追加読み込みする様に実装する
                                        if (!isLoading && lastVisibleItem >= totalCount -6) { // 読み込み中でない、かつ、現在のスクロール位置から下に5件見えていないアイテムがある
                                                updateData(true)
                                        }
                                }
                        })
                }
                swipeRefreshLayout.setOnRefreshListener {
                        updateData()
                }
                updateData()
        }

        fun updateView() { // お気に入りが削除されたときの処理（Activityからコールされる
                recyclerview.adapter?.notifyDataSetChanged() // RecylerViewのAdapterに対して再描画のリクエストをする
        }

        private fun updateData(isAdd: Boolean = false) {
                if (isLoading) {
                        return
                } else {
                        isLoading = true
                }
                if (isAdd) {
                        page++
                } else {
                        page = 0
                }
                val start = page * COUNT + 1

                val url = StringBuilder()
                        .append(getString(R.string.base_url)) // https://webservice.recruit.co.jp/hotpepper/gourmet/v1/
                        .append("?key=").append(getString(R.string.api_key)) // Apiを使うためのApiKey
                        .append("&start=").append(start) // 何件目からのデータを取得するか
                        .append("&count=").append(COUNT) // 1回で20件取得する
                        .append("&keyword=").append(getString(R.string.api_keyword)) // お店の検索ワード。ここでは例として「ランチ」を検索
                        .append("&format=json") // ここで利用しているAPIは戻りの形をxmlかjsonか選択することができる。Androidで扱う場合はxmlよりもjsonの方が扱いやすいので、jsonを選択
                        .toString()
                val client = OkHttpClient.Builder()
                        .addInterceptor(HttpLoggingInterceptor().apply {
                                level = HttpLoggingInterceptor.Level.BODY // ログに通信の詳細を出す
                        })
                        .build()
                val request = Request.Builder()
                        .url(url)
                        .build()
                client.newCall(request).enqueue(object: Callback { // Callbackインタフェースを実装
                        override fun onFailure(call: Call, e:IOException) { // Error時の処理
                                e.printStackTrace()
                                handler.post { //API通信スレッドからUI描画スレッドへ渡す
                                        updateRecyclerView(listOf(), isAdd) // 失敗時は、空のリストで更新
                                }
                                isLoading = false // 読み込み中フラグを折る
                        }
                        override fun onResponse(call: Call, response: Response) { // 成功時の処理
                                var list = listOf<Shop>()
                                response.body?.string()?.also {
                                        val apiResponse =
                                                Gson().fromJson(it, ApiResponse::class.java) // JSONからApiResponseへの変換
                                        list = apiResponse.results.shop
                                }
                                handler.post {
                                        updateRecyclerView(list, isAdd)
                                }
                                isLoading = false // 読み込み中フラグを折る

                                Log.v("DEBUG", list[0].name + ":" + list[0].address)
                        }
                })
        }

        private fun updateRecyclerView(list: List<Shop>, isAdd: Boolean) {
                if (isAdd) {
                        apiAdapter.add(list)
                } else {
                        apiAdapter.refresh(list)
                }
                swipeRefreshLayout.isRefreshing = false // SwipeRefreshLayoutのくるくるを消す
        }

        companion object {
                private const val COUNT = 20 // 1回のAPIで取得する件数
        }

}