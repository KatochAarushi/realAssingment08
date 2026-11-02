package edu.charlotte.assignment07.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import edu.charlotte.assignment07.databinding.FragmentAddTaskBinding;
import edu.charlotte.assignment07.models.Priority;
import edu.charlotte.assignment07.models.Task;

public class AddTaskFragment extends Fragment {

    private Priority selectedPriority;
    private String selectedCategory;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    public AddTaskFragment() {
        // Required empty public constructor
    }

    public void setSelectedPriority(Priority selectedPriority) {
        this.selectedPriority = selectedPriority;

        if (binding != null) {
            binding.textViewPriority.setText(selectedPriority.getName());
        }
    }

    public void setSelectedCategory(String selectedCategory) {
        this.selectedCategory = selectedCategory;

        if (binding != null) {
            binding.textViewCategory.setText(selectedCategory);
        }
    }

    FragmentAddTaskBinding binding;

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentAddTaskBinding.inflate(
                inflater,
                container,
                false
        );

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        binding.buttonSelectCategory.setOnClickListener(v ->
                mListener.gotoSelectCategory()
        );

        binding.buttonSelectPriority.setOnClickListener(v ->
                mListener.gotoSelectPriority()
        );

        binding.buttonSubmit.setOnClickListener(v -> {

            String name = binding.editTextName
                    .getText()
                    .toString()
                    .trim();

            if (name.isEmpty()) {
                Toast.makeText(
                        getActivity(),
                        "Enter Name !!",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            if (selectedCategory == null) {
                Toast.makeText(
                        getActivity(),
                        "Select Category !!",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            if (selectedPriority == null) {
                Toast.makeText(
                        getActivity(),
                        "Select Priority !!",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            saveTask(name);
        });

        binding.textViewCategory.setText(
                selectedCategory != null
                        ? selectedCategory
                        : "N/A"
        );

        binding.textViewPriority.setText(
                selectedPriority != null
                        ? selectedPriority.getName()
                        : "N/A"
        );
    }

    private void saveTask(String name) {

        if (auth.getCurrentUser() == null) {
            return;
        }

        String uid = auth.getCurrentUser().getUid();

        Task task = new Task(
                null,
                name,
                selectedCategory,
                selectedPriority.getName(),
                uid
        );

        db.collection("tasks")
                .add(task)
                .addOnSuccessListener(documentReference -> {

                    task.setId(documentReference.getId());

                    Toast.makeText(
                            getActivity(),
                            "Task Added",
                            Toast.LENGTH_SHORT
                    ).show();

                    mListener.onTaskAdded(task);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(
                                getActivity(),
                                "Failed to save task",
                                Toast.LENGTH_SHORT
                        ).show()
                );
    }

    AddTaskListener mListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof AddTaskListener) {
            mListener = (AddTaskListener) context;
        } else {
            throw new RuntimeException(
                    context.toString()
                            + " must implement AddTaskListener"
            );
        }
    }

    public interface AddTaskListener {
        void gotoSelectPriority();
        void gotoSelectCategory();
        void onTaskAdded(Task task);
        void onCancelSelection();
    }
}