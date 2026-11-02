/*
 * Assignment 8
 * File Name: CreateAccountFragment.java
 * Name: Aarushi Katoch
 */

package edu.charlotte.assignment07.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import edu.charlotte.assignment07.databinding.FragmentCreateAccountBinding;

public class CreateAccountFragment extends Fragment {

    private FragmentCreateAccountBinding binding;
    private FirebaseAuth mAuth;
    private RegisterListener mListener;

    public CreateAccountFragment() {
        // Required empty constructor.
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCreateAccountBinding.inflate(
                inflater, container, false
        );
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

        binding.buttonCancel.setOnClickListener(v ->
                mListener.gotoLoginAccount()
        );

        binding.buttonSubmit.setOnClickListener(v -> {
            String name = binding.editTextText.getText()
                    .toString().trim();

            String email = binding.editTextEmailAddress.getText()
                    .toString().trim();

            String password = binding.editTextPassword.getText()
                    .toString();

            if (name.isEmpty()) {
                showError("Full name is required.");
                return;
            }

            if (email.isEmpty()) {
                showError("Email is required.");
                return;
            }

            if (password.isEmpty()) {
                showError("Password is required.");
                return;
            }

            setBusy(true);

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            if (binding == null) {
                                return;
                            }

                            setBusy(false);
                            showError(task.getException() != null
                                    ? task.getException().getMessage()
                                    : "Unable to create account.");
                            return;
                        }

                        FirebaseUser user = task.getResult().getUser();

                        UserProfileChangeRequest profile =
                                new UserProfileChangeRequest.Builder()
                                        .setDisplayName(name)
                                        .build();

                        user.updateProfile(profile)
                                .addOnCompleteListener(profileTask -> {
                                    if (binding == null) {
                                        return;
                                    }

                                    if (profileTask.isSuccessful()) {
                                        mListener.onCreateAccountSuccess();
                                    } else {
                                        setBusy(false);
                                        showProfileRetry(user, profile);
                                    }
                                });
                    });
        });
    }

    private void showProfileRetry(
            FirebaseUser user,
            UserProfileChangeRequest profile
    ) {
        if (!isAdded() || binding == null) {
            return;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Name Not Saved")
                .setMessage(
                        "Your account was created, but your name "
                                + "could not be saved. Tap Retry "
                                + "to save your name."
                )
                .setCancelable(false)
                .setPositiveButton("Retry", (dialog, which) -> {
                    setBusy(true);

                    user.updateProfile(profile)
                            .addOnCompleteListener(task -> {
                                if (binding == null) {
                                    return;
                                }

                                if (task.isSuccessful()) {
                                    mListener.onCreateAccountSuccess();
                                } else {
                                    setBusy(false);
                                    showProfileRetry(user, profile);
                                }
                            });
                })
                .show();
    }

    private void setBusy(boolean busy) {
        binding.buttonSubmit.setEnabled(!busy);
        binding.buttonCancel.setEnabled(!busy);
    }

    private void showError(String message) {
        if (!isAdded() || binding == null) {
            return;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Registration Error")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof RegisterListener) {
            mListener = (RegisterListener) context;
        } else {
            throw new RuntimeException(
                    context + " must implement RegisterListener"
            );
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public interface RegisterListener {
        void onCreateAccountSuccess();
        void gotoLoginAccount();
    }
}