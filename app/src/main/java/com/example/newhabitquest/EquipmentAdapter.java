package com.example.newhabitquest;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class EquipmentAdapter extends RecyclerView.Adapter<EquipmentAdapter.EquipmentViewHolder> {
    private List<Equipment> equipmentList;
    private OnEquipmentClickListener listener;

    public interface OnEquipmentClickListener {
        void onBuyClick(Equipment equipment);
        void onActivateClick(Equipment equipment);
    }

    public EquipmentAdapter(List<Equipment> equipmentList, OnEquipmentClickListener listener) {
        this.equipmentList = equipmentList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public EquipmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_equipment, parent, false);
        return new EquipmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EquipmentViewHolder holder, int position) {
        Equipment equipment = equipmentList.get(position);
        holder.bind(equipment, listener);
    }

    @Override
    public int getItemCount() {
        return equipmentList.size();
    }

    public void updateList(List<Equipment> newList) {
        this.equipmentList = newList;
        notifyDataSetChanged();
    }

    static class EquipmentViewHolder extends RecyclerView.ViewHolder {
        private TextView nameText, descriptionText, priceText, quantityText, statusText;
        private Button actionButton, activateButton;

        public EquipmentViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.equipment_name);
            descriptionText = itemView.findViewById(R.id.equipment_description);
            priceText = itemView.findViewById(R.id.equipment_price);
            quantityText = itemView.findViewById(R.id.equipment_quantity);
            statusText = itemView.findViewById(R.id.equipment_status);
            actionButton = itemView.findViewById(R.id.equipment_action_btn);
            activateButton = itemView.findViewById(R.id.equipment_activate_btn);
        }

        public void bind(Equipment equipment, OnEquipmentClickListener listener) {
            nameText.setText(equipment.getName());
            descriptionText.setText(equipment.getDescription());

            if (equipment.getPrice() > 0) {
                priceText.setText("Cena: " + equipment.getPrice() + " novčića");
                priceText.setVisibility(View.VISIBLE);
            } else {
                priceText.setText("Dobija se samo u borbama");
                priceText.setVisibility(View.VISIBLE);
            }

            if (equipment.getType().equals("napici") && equipment.isOwned()) {
                quantityText.setVisibility(View.VISIBLE);
                quantityText.setText("Količina: " + equipment.getQuantity());
            } else {
                quantityText.setVisibility(View.GONE);
            }

            // Status text - removed all "Aktivno" labels
            // Simply hide the status text completely to remove green "AKTIVNO" labels
            statusText.setVisibility(View.GONE);

            // Button logic
            actionButton.setVisibility(View.GONE);
            activateButton.setVisibility(View.GONE);
            if (!equipment.isOwned() && equipment.getPrice() > 0) {
                actionButton.setText("Kupi");
                actionButton.setVisibility(View.VISIBLE);
                actionButton.setEnabled(true);
                actionButton.setOnClickListener(v -> listener.onBuyClick(equipment));
            } else if (equipment.isOwned() && equipment.getType().equals("napici") && equipment.getQuantity() > 0 && !equipment.isActive()) {
                activateButton.setText("Aktiviraj");
                activateButton.setVisibility(View.VISIBLE);
                activateButton.setEnabled(true);
                activateButton.setOnClickListener(v -> listener.onActivateClick(equipment));
            } else if (equipment.isOwned() && equipment.getType().equals("odeca") && !equipment.isActive()) {
                activateButton.setText("Aktiviraj");
                activateButton.setVisibility(View.VISIBLE);
                activateButton.setEnabled(true);
                activateButton.setOnClickListener(v -> listener.onActivateClick(equipment));
            } else if (equipment.getType().equals("oruzje") && !equipment.isOwned()) {
                actionButton.setText("Nedostupno");
                actionButton.setVisibility(View.VISIBLE);
                actionButton.setEnabled(false);
                actionButton.setOnClickListener(null);
            }
        }
    }
}
