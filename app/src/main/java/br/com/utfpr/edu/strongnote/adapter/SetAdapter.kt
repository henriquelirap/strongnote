package br.com.utfpr.edu.strongnote.adapter

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import br.com.utfpr.edu.strongnote.databinding.ItemSetsBinding
import br.com.utfpr.edu.strongnote.model.SetModel

class SetAdapter(
    private val context: Context,
    private val setSelected: (SetModel, Int, String) -> Unit
) : ListAdapter<SetModel, SetAdapter.MyViewHolder>(DIFF_CALLBACK) {

    companion object {
        val DELETE: String = "DELETE"

        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<SetModel>() {
            override fun areItemsTheSame(
                oldItem: SetModel,
                newItem: SetModel
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: SetModel,
                newItem: SetModel
            ): Boolean {
                return oldItem == newItem
            }
        }
    }

    inner class MyViewHolder(val binding: ItemSetsBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemSetsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val set = getItem(position)
        holder.binding.setEdit.setText(set.set.toString())
        holder.binding.repEdit.setText(set.repetitions.toString())
        holder.binding.kgEdit.setText(set.kilograms.toString())
        holder.binding.btnDelete.setOnClickListener {
            setSelected(set, position, DELETE)
        }

        holder.binding.setEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                set.set = s.toString()
                setSelected(set, holder.adapterPosition, "")
            }
        })

        holder.binding.repEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                set.repetitions = s.toString()
                setSelected(set, holder.adapterPosition, "")
            }
        })

        holder.binding.kgEdit.addTextChangedListener(
            object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    set.kilograms = s.toString()
                    setSelected(set, holder.adapterPosition, "")
                }
            })


    }
}