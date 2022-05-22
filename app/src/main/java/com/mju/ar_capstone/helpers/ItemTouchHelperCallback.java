package com.mju.ar_capstone.helpers;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.mju.ar_capstone.adapter.UserListAdapter;

public class ItemTouchHelperCallback extends ItemTouchHelper.Callback {

    private UserListAdapter adapter;
    public ItemTouchHelperCallback(UserListAdapter userListAdapter){this.adapter = userListAdapter;}

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView,
                                @NonNull RecyclerView.ViewHolder viewHolder) {
        int drag_flags = ItemTouchHelper.START|ItemTouchHelper.END|ItemTouchHelper.UP|ItemTouchHelper.DOWN;
        int swipe_flasg = ItemTouchHelper.START|ItemTouchHelper.END;
        return makeMovementFlags(drag_flags,0);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView,
                          @NonNull RecyclerView.ViewHolder viewHolder,
                          @NonNull RecyclerView.ViewHolder target) {
        return adapter.onItemMove(viewHolder.getAdapterPosition(),target.getAdapterPosition());
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
//        listener.onItemSwipe(viewHolder.getAdapterPosition());
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return true;
    }
}
