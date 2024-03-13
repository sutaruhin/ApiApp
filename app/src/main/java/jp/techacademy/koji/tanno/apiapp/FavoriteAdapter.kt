package jp.techacademy.koji.tanno.apiapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.solver.state.State.Constraint
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class FavoriteAdapter(private val context: Context): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // お気に入り登録したShopを格納
    private val items = mutableListOf<FavoriteShop>()

    // お気に入り画面から削除するときのコールバック（ApiFragmentへ通知するメソッド）
    var onClickDeleteFavorite: ((FavoriteShop) -> Unit)? = null   // 引数FavoriteShop型、戻りUnit型のNullable関数を定義。初期値をnullにしておく。

    // Itemを押したときのメソッド
    var onClickItem: ((String,String,String,String,String) -> Unit)? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType) {
            // viewTypeがVIEW_TYPE_EMPTY（つまり、お気に入り登録が0件）の場合
            VIEW_TYPE_EMPTY -> EmptyViewHolder(LayoutInflater.from(context).inflate(R.layout.recycler_favorite_empty, parent, false))
            // 上記以外（つまり、1件以上のお気に入りが登録されている場合
            else -> FavoriteItemViewHolder(LayoutInflater.from(context).inflate(R.layout.recycler_favorite, parent, false))
        }
    }

    // 更新用のメソッド
    fun refresh(list: List<FavoriteShop>) {
        items.apply {
            clear() // itemを空にする
            addAll(list) // itemsにlistを全て追加する
        }
        notifyDataSetChanged() // recyclerViewを再描画させる
    }

    override fun getItemCount(): Int {
        return if (items.isEmpty()) 1 else items.size // お気に入りが1件もない時に「お気に入りはありません」を出すため
    }

    // onCreateViewHolderの第二引数はここで決める。条件によってViewTypeを返すようにすると、１つのRecyclerViewで様々なViewがある物が作れる
    override fun getItemViewType(position: Int): Int {
        return if (items.isEmpty()) VIEW_TYPE_EMPTY else VIEW_TYPE_ITEM
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is FavoriteItemViewHolder) {
            updateFavoriteItemViewHolder(holder, position)
        }
    }

    // ViewHolder内のUI部品に値などをセット
    private fun updateFavoriteItemViewHolder(holder: FavoriteItemViewHolder, position: Int) {
        val data = items[position]
        holder.apply {
            rootView.apply {
                setBackgroundColor(ContextCompat.getColor(context, if (position % 2 == 0) android.R.color.white else android.R.color.darker_gray)) // 偶数番目と奇数番目で背景色を変更させる
                setOnClickListener {
                    onClickItem?.invoke(data.url, data.id, data.name, data.address, data.imageUrl)
                }

            }
            nameTextView.text = data.name
            Picasso.get().load(data.imageUrl).into(imageView)
            favoriteImageView.setOnClickListener {
                onClickDeleteFavorite?.invoke(data)
                notifyItemChanged(position)
            }
        }

    }

    // お気に入りが登録されているときのリスト
    class FavoriteItemViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val rootView: ConstraintLayout = view.findViewById(R.id.rootView)
        val nameTextView: TextView = view.findViewById(R.id.nameTextView)
        val imageView: ImageView = view.findViewById(R.id.imageView)
        val favoriteImageView: ImageView = view.findViewById(R.id.favoriteImageView)
    }

    // お気に入り登録がまだ行われていないとき
    class EmptyViewHolder(view: View): RecyclerView.ViewHolder(view)


    companion object {
        // viewの種類を表現する定義
        private const val VIEW_TYPE_ITEM = 0
        private const val VIEW_TYPE_EMPTY = 1
    }

}
