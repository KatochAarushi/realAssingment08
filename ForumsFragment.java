/*
 * Assignment 8
 * File Name: ForumsFragment.java
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.WriteBatch;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import edu.charlotte.assignment07.databinding.FragmentForumsBinding;
import edu.charlotte.assignment07.databinding.ForumListItemBinding;
import edu.charlotte.assignment07.models.Forum;

public class ForumsFragment extends Fragment {

    private FragmentForumsBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private ListenerRegistration registration;
    private ForumsListener mListener;

    private final ArrayList<Forum> forums = new ArrayList<>();
    private ForumsAdapter adapter;

    public ForumsFragment() {
        // Required empty constructor.
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentForumsBinding.inflate(
                inflater, container, false
        );
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        adapter = new ForumsAdapter();

        binding.recyclerViewForums.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );
        binding.recyclerViewForums.setAdapter(adapter);

        binding.buttonNewForum.setOnClickListener(v ->
                mListener.gotoNewForum()
        );

        binding.buttonLogout.setOnClickListener(v -> {
            stopListening();
            auth.signOut();
            mListener.logoutGotoLogin();
        });

        loadForums();
    }

    private void loadForums() {
        registration = db.collection("forums")
                .addSnapshotListener((value, error) -> {
                    if (binding == null) {
                        return;
                    }

                    if (error != null) {
                        showError(error.getMessage());
                        return;
                    }

                    if (value == null) {
                        return;
                    }

                    forums.clear();

                    for (DocumentSnapshot document
                            : value.getDocuments()) {
                        Forum forum = document.toObject(Forum.class);

                        if (forum != null) {
                            forum.setId(document.getId());
                            forums.add(forum);
                        }
                    }

                    adapter.notifyDataSetChanged();
                });
    }

    private void toggleLike(Forum forum) {
        if (auth.getCurrentUser() == null) {
            return;
        }

        String uid = auth.getCurrentUser().getUid();

        FieldValue change;

        if (forum.getLikes().contains(uid)) {
            change = FieldValue.arrayRemove(uid);
        } else {
            change = FieldValue.arrayUnion(uid);
        }

        db.collection("forums")
                .document(forum.getId())
                .update("likes", change)
                .addOnFailureListener(error ->
                        showError(error.getMessage())
                );
    }

    private void deleteForum(Forum forum) {
        if (auth.getCurrentUser() == null) {
            return;
        }

        String uid = auth.getCurrentUser().getUid();

        if (!uid.equals(forum.getUserId())) {
            return;
        }

        DocumentReference forumRef = db.collection("forums")
                .document(forum.getId());

        deleteCommentsThenForum(forumRef);
    }

    private void deleteCommentsThenForum(
            DocumentReference forumRef
    ) {
        // Delete comments in small batches before deleting the forum.
        forumRef.collection("comments")
                .limit(400)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.isEmpty()) {
                        forumRef.delete()
                                .addOnFailureListener(error ->
                                        showError(error.getMessage())
                                );
                        return;
                    }

                    WriteBatch batch = db.batch();

                    for (DocumentSnapshot comment
                            : snapshot.getDocuments()) {
                        batch.delete(comment.getReference());
                    }

                    batch.commit()
                            .addOnSuccessListener(unused ->
                                    deleteCommentsThenForum(forumRef)
                            )
                            .addOnFailureListener(error ->
                                    showError(error.getMessage())
                            );
                })
                .addOnFailureListener(error ->
                        showError(error.getMessage())
                );
    }

    private void showError(String message) {
        if (!isAdded() || binding == null) {
            return;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    private void stopListening() {
        if (registration != null) {
            registration.remove();
            registration = null;
        }
    }

    @Override
    public void onDestroyView() {
        stopListening();
        super.onDestroyView();
        binding = null;
    }

    class ForumsAdapter extends
            RecyclerView.Adapter<ForumsAdapter.ForumViewHolder> {

        @NonNull
        @Override
        public ForumViewHolder onCreateViewHolder(
                @NonNull ViewGroup parent,
                int viewType
        ) {
            ForumListItemBinding itemBinding =
                    ForumListItemBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    );

            return new ForumViewHolder(itemBinding);
        }

        @Override
        public void onBindViewHolder(
                @NonNull ForumViewHolder holder,
                int position
        ) {
            holder.bind(forums.get(position));
        }

        @Override
        public int getItemCount() {
            return forums.size();
        }

        class ForumViewHolder extends RecyclerView.ViewHolder {

            private final ForumListItemBinding itemBinding;

            public ForumViewHolder(
                    ForumListItemBinding itemBinding
            ) {
                super(itemBinding.getRoot());
                this.itemBinding = itemBinding;
            }

            public void bind(Forum forum) {
                itemBinding.textViewTitle.setText(
                        forum.getTitle()
                );

                itemBinding.textViewCreator.setText(
                        forum.getUserName()
                );

                itemBinding.textViewDescription.setText(
                        forum.getDescription()
                );

                itemBinding.textViewLikes.setText(
                        forum.getLikes().size() + " Likes"
                );

                if (forum.getCreatedAt() != null) {
                    SimpleDateFormat format =
                            new SimpleDateFormat(
                                    "MM/dd/yyyy h:mm a",
                                    Locale.US
                            );

                    itemBinding.textViewDate.setText(
                            format.format(
                                    forum.getCreatedAt().toDate()
                            )
                    );
                } else {
                    itemBinding.textViewDate.setText("");
                }

                String uid = auth.getCurrentUser() == null
                        ? ""
                        : auth.getCurrentUser().getUid();

                boolean ownForum =
                        !uid.isEmpty()
                                && uid.equals(forum.getUserId());

                itemBinding.buttonDelete.setVisibility(
                        ownForum ? View.VISIBLE : View.GONE
                );

                boolean liked = forum.getLikes().contains(uid);

                itemBinding.buttonLike.setText(
                        liked ? "Unlike" : "Like"
                );

                itemBinding.buttonLike.setOnClickListener(v ->
                        toggleLike(forum)
                );

                itemBinding.buttonDelete.setOnClickListener(v ->
                        deleteForum(forum)
                );

                itemBinding.getRoot().setOnClickListener(v ->
                        mListener.gotoForum(forum.getId())
                );
            }
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof ForumsListener) {
            mListener = (ForumsListener) context;
        } else {
            throw new RuntimeException(
                    context + " must implement ForumsListener"
            );
        }
    }

    public interface ForumsListener {
        void gotoNewForum();
        void gotoForum(String forumId);
        void logoutGotoLogin();
    }
}