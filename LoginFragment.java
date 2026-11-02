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

import edu.charlotte.assignment07.databinding.FragmentLoginBinding;

public class LoginFragment extends Fragment {

    public LoginFragment() {

    }

    FragmentLoginBinding binding;
    FirebaseAuth mAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

        binding.buttonCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.gotoCreateAccount();
            }
        });

        binding.buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = binding.editTextEmailAddress
                        .getText()
                        .toString()
                        .trim();

                String password = binding.editTextPassword
                        .getText()
                        .toString();

                if (email.isEmpty()) {
                    new AlertDialog.Builder(requireContext())
                            .setTitle("Missing Email")
                            .setMessage("Email is required.")
                            .setPositiveButton("OK", null)
                            .show();

                    return;
                }

                if (password.isEmpty()) {
                    new AlertDialog.Builder(requireContext())
                            .setTitle("Missing Password")
                            .setMessage("Password is required.")
                            .setPositiveButton("OK", null)
                            .show();

                    return;
                }

                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {

                            if (task.isSuccessful()) {

                                mListener.onLoginSuccess();

                            } else {

                                String message = "Unable to login.";

                                if (task.getException() != null) {
                                    message = task.getException().getMessage();
                                }

                                new AlertDialog.Builder(requireContext())
                                        .setTitle("Login Failed")
                                        .setMessage(message)
                                        .setPositiveButton("OK", null)
                                        .show();
                            }
                        });
            }
        });
    }

    LoginListener mListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            mListener = (LoginListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(
                    context.toString() + " must implement LoginListener"
            );
        }
    }

    public interface LoginListener {
        void onLoginSuccess();
        void gotoCreateAccount();
    }
}