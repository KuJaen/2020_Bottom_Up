package com.ollive.loopstation.waveform;

import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ollive.loopstation.R;
import com.ollive.loopstation.databinding.TrackBinding;

public class TrackViewHolder extends RecyclerView.ViewHolder {
    TrackBinding binding;
    Button btnR;
    Button btnS;
    Button btnL;

    BoardManager boardManager;

    public TrackViewHolder(@NonNull TrackBinding binding) {
        super(binding.getRoot());
        this.binding = binding;

        btnR = (Button) itemView.findViewById(R.id.setRecord);
        btnS = (Button) itemView.findViewById(R.id.setStop);
        btnL = (Button) itemView.findViewById(R.id.setLoop);
        boardManager = (BoardManager) itemView.findViewById(R.id.BoardManaverView);
    }

    public void setViewModel(TrackViewModel model){
        binding.setModel(model);
        binding.executePendingBindings();
    }

    public Button getBtnR() {
        return btnR;
    }
    public Button getBtnS() {
        return btnS;
    }
    public Button getBtnL() {
        return btnL;
    }

    public BoardManager getBoardManager() {
        return boardManager;
    }
}
