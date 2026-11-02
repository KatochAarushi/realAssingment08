/*
 * Assignment 8
 * File Name: MainActivity.java
 * Name: Aarushi Katoch
 */

package edu.charlotte.assignment07;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import edu.charlotte.assignment07.fragments.CreateAccountFragment;
import edu.charlotte.assignment07.fragments.ForumsFragment;
import edu.charlotte.assignment07.fragments.LoginFragment;

public class MainActivity extends AppCompatActivity
        implements LoginFragment.LoginListener,
        CreateAccountFragment.RegisterListener,
        ForumsFragment.ForumsListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main),
                (v, insets) -> {
                    Insets systemBars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                    );

                    v.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom
                    );

                    return insets;
                }
        );

        if (savedInstanceState == null) {
            showScreen(new LoginFragment());
        }
    }

    private void showScreen(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main, fragment)
                .commit();
    }

    @Override
    public void onLoginSuccess() {
        showScreen(new ForumsFragment());
    }

    @Override
    public void onCreateAccountSuccess() {
        showScreen(new ForumsFragment());
    }

    @Override
    public void gotoCreateAccount() {
        showScreen(new CreateAccountFragment());
    }

    @Override
    public void gotoLoginAccount() {
        showScreen(new LoginFragment());
    }

    @Override
    public void logoutGotoLogin() {
        getSupportFragmentManager().popBackStackImmediate(
                null,
                FragmentManager.POP_BACK_STACK_INCLUSIVE
        );

        showScreen(new LoginFragment());
    }

    @Override
    public void gotoNewForum() {
        // Replace with navigation to your groupmate's screen.
        Toast.makeText(
                this,
                "New Forum screen will be added later.",
                Toast.LENGTH_SHORT
        ).show();
    }

    @Override
    public void gotoForum(String forumId) {
        // Pass forumId to your groupmate's forum details screen.
        Toast.makeText(
                this,
                "Forum details screen will be added later.",
                Toast.LENGTH_SHORT
        ).show();
    }
}