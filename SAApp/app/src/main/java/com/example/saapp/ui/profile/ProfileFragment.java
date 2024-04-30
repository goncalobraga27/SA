package com.example.saapp.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.saapp.R;
import com.example.saapp.databinding.FragmentProfileBinding;

import org.w3c.dom.Text;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private TextView textViewUserRole;
    private TextView profilePoints;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ProfileViewModel profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        profileViewModel.getUserEmail().observe(getViewLifecycleOwner(), email -> binding.editTextEmailProfile.setText(email));
        textViewUserRole = root.findViewById(R.id.TextViewRoleProfile);
        profileViewModel.getUserRole().observe(getViewLifecycleOwner(), role -> {
            if (role != null) {
                textViewUserRole.setText(role);
            } else {
                textViewUserRole.setText("Role do utilizador não encontrado");
            }
        });
        profilePoints = root.findViewById(R.id.profilePoints);
        profileViewModel.getUserPontos().observe(getViewLifecycleOwner(), points -> {
            if (points != null) {
                profilePoints.setText(Double.toString(points));
            } else {
                profilePoints.setText("Não tem pontos");
            }
        });
        profileViewModel.getPasswordResetEmailStatus().observe(getViewLifecycleOwner(), this::showToast);

        binding.buttonLogout.setOnClickListener(v -> profileViewModel.logout());
        binding.buttonResetPass.setOnClickListener(v -> profileViewModel.sendPasswordResetEmail());


        return root;
    }

    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}