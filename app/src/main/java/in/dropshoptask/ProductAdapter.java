package in.dropshoptask;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import in.dropshoptask.databinding.ProductItemBinding;
import in.dropshoptask.modal.ProductData;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductItem> {

    private List<ProductData> productList;

    @NonNull
    @Override
    public ProductItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ProductItemBinding productItemBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.product_item, parent, false);
        return new ProductItem(productItemBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductItem holder, int position) {

        ProductData item = productList.get(position);
        holder.productItemBinding.setProduct(item);

    }

    void setProductList(List<ProductData> list) {
        this.productList = list;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (productList != null) {
            return productList.size();
        } else {
            return 0;
        }
    }

    class ProductItem extends RecyclerView.ViewHolder {
        ProductItemBinding productItemBinding;
        ProductItem(@NonNull ProductItemBinding itemBinding) {
            super(itemBinding.getRoot());
            this.productItemBinding = itemBinding;
        }
    }

}
