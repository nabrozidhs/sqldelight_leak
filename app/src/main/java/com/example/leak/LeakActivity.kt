package com.example.leak

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.PagingConfig
import androidx.paging.PagingDataAdapter
import androidx.paging.cachedIn
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import app.cash.paging.Pager
import app.cash.sqldelight.paging3.QueryPagingSource
import com.example.leak.databinding.FragmentLeakBinding
import com.example.leak.db.SomeTable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class LeakActivity : AppCompatActivity() {

    val db by lazy { DatabaseProvider.instance(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = FragmentLeakBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = Adapter()
        binding.recyclerView.adapter = adapter

        val pager = Pager(
            config = PagingConfig(pageSize = 20),
            pagingSourceFactory = {
                QueryPagingSource(
                    countQuery = db.someTableQueries.itemsCount(),
                    transacter = db.someTableQueries,
                    queryProvider = { limit, offset ->
                        db.someTableQueries.items(limit = limit, offset = offset)
                    },
                    context = Dispatchers.IO,
                )
            },
        ).flow.cachedIn(lifecycleScope)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                pager.collectLatest(adapter::submitData)
            }
        }
    }

    class Adapter : PagingDataAdapter<SomeTable, Adapter.ViewHolder>(
        object : DiffUtil.ItemCallback<SomeTable>() {
            override fun areItemsTheSame(oldItem: SomeTable, newItem: SomeTable): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: SomeTable, newItem: SomeTable): Boolean =
                oldItem == newItem
        }
    ) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                android.R.layout.simple_list_item_1,
                parent,
                false,
            )
        )

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            getItem(position)?.let(holder::bind)
        }

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

            fun bind(someTable: SomeTable) {
                itemView.findViewById<TextView>(android.R.id.text1).text = someTable.text
            }
        }
    }
}