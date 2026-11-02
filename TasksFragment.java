package edu.charlotte.assignment07.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;

import edu.charlotte.assignment07.databinding.FragmentTasksBinding;
import edu.charlotte.assignment07.databinding.TaskListItemBinding;
import edu.charlotte.assignment07.models.Task;

public class TasksFragment extends Fragment {

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private ListenerRegistration listenerRegistration;

    private final ArrayList<Task> taskList = new ArrayList<>();

    FragmentTasksBinding binding;
    TasksAdapter adapter;

    public TasksFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentTasksBinding.inflate(inflater, container, false);
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

        adapter = new TasksAdapter();

        binding.recyclerView.setLayoutManager(
                new LinearLayoutManager(getContext())
        );

        binding.recyclerView.setAdapter(adapter);

        loadTasks();

        binding.buttonAddNew.setOnClickListener(v ->
                mListener.gotoAddTask()
        );

        binding.buttonLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            mListener.logoutGotoLogin();
        });
    }

    private void loadTasks() {

        if (auth.getCurrentUser() == null) {
            return;
        }

        String uid = auth.getCurrentUser().getUid();

        listenerRegistration =
                db.collection("tasks")
                        .whereEqualTo("userId", uid)
                        .addSnapshotListener((value, error) -> {

                            if (error != null || value == null) {
                                return;
                            }

                            taskList.clear();

                            for (DocumentSnapshot document :
                                    value.getDocuments()) {

                                Task task =
                                        document.toObject(Task.class);

                                if (task != null) {
                                    task.setId(document.getId());
                                    taskList.add(task);
                                }
                            }

                            adapter.notifyDataSetChanged();
                        });
    }

    private void deleteTask(Task task) {

        db.collection("tasks")
                .document(task.getId())
                .delete();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
    }

    class TasksAdapter
            extends RecyclerView.Adapter<TasksAdapter.TaskViewHolder> {

        @NonNull
        @Override
        public TaskViewHolder onCreateViewHolder(
                @NonNull ViewGroup parent,
                int viewType
        ) {

            TaskListItemBinding itemBinding =
                    TaskListItemBinding.inflate(
                            getLayoutInflater(),
                            parent,
                            false
                    );

            return new TaskViewHolder(itemBinding);
        }

        @Override
        public int getItemCount() {
            return taskList.size();
        }

        @Override
        public void onBindViewHolder(
                @NonNull TaskViewHolder holder,
                int position
        ) {
            holder.bind(taskList.get(position));
        }

        class TaskViewHolder extends RecyclerView.ViewHolder {

            TaskListItemBinding itemBinding;

            public TaskViewHolder(
                    TaskListItemBinding itemBinding
            ) {
                super(itemBinding.getRoot());
                this.itemBinding = itemBinding;
            }

            public void bind(Task task) {

                itemBinding.textViewName.setText(
                        task.getName()
                );

                itemBinding.textViewCategory.setText(
                        task.getCategory()
                );

                itemBinding.textViewPriority.setText(
                        task.getPriority()
                );

                itemBinding.imageView.setOnClickListener(v ->
                        deleteTask(task)
                );
            }
        }
    }

    TasksListener mListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof TasksListener) {
            mListener = (TasksListener) context;
        } else {
            throw new RuntimeException(
                    context +
                            " must implement TasksListener"
            );
        }
    }

    public interface TasksListener {
        void gotoAddTask();
        void logoutGotoLogin();
    }
}