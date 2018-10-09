package pl.edu.mimuw.tuto;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import pl.edu.mimuw.tuto.modules.my_account.MyAccountFragment;
import pl.edu.mimuw.tuto.modules.my_sessions.MySessionsFragment;
import pl.edu.mimuw.tuto.modules.start.ChangePasswordFragment;
import pl.edu.mimuw.tuto.modules.start.LoginFragment;
import pl.edu.mimuw.tuto.modules.start.ResendLinkFragment;
import pl.edu.mimuw.tuto.modules.start.SignupFragment;
import pl.edu.mimuw.tuto.modules.wall.WallFragment;
import pl.edu.mimuw.tuto.notifications.NotificationsService;

public class MainActivity extends AppCompatActivity
    implements NavigationView.OnNavigationItemSelectedListener {

  public final static String LOGIN_PREFERENCES = "LoginSharedPreferences";
  public final static String IS_LOGGED_IN_KEY = "IsLoggedIn";
  public final static String EMAIL_KEY = "Email";
  public final static String SAVED_FRAGMENT_KEY = "Fragment";

  private final static String MY_SESSIONS = "MySessions";
  private final static String SAVED_SESSIONS = "SavedSessions";
  private final static String ARCHIVE_SESSIONS = "ArchiveSessions";


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    if (savedInstanceState != null && savedInstanceState.containsKey(SAVED_FRAGMENT_KEY)) {
      showSavedFragment(savedInstanceState);
    } else if (isLoggedIn()) {
      showWallFragment();
    } else {
      showLoginFragment();
    }

    updateNavigationView();

    // See comment at the top of NotificationsService class.
    if (savedInstanceState == null) {
      startService(new Intent(this, NotificationsService.class));
    }
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    Fragment visibleFragment = null;

    String[] mainFragmentTags = {
        SignupFragment.TAG,
        ChangePasswordFragment.TAG,
        ResendLinkFragment.TAG,
        WallFragment.TAG,
        MyAccountFragment.TAG,
        LoginFragment.TAG,
        MySessionsFragment.MY_SESSIONS,
        MySessionsFragment.SAVED_SESSIONS,
        MySessionsFragment.ARCHIVE_SESSIONS
    };

    for (String tag : mainFragmentTags) {
      Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
      if (fragment != null && fragment.isVisible()) {
        visibleFragment = fragment;
      }
    }

    if (visibleFragment != null) {
      Log.d("MAIN", "Saved fragment.");
      getSupportFragmentManager().putFragment(outState, SAVED_FRAGMENT_KEY, visibleFragment);
    }
  }

  private void showLoginFragment() {
    FragmentManager fragmentManager = getSupportFragmentManager();

    LoginFragment fragment = LoginFragment.getInstance(fragmentManager);
    fragmentManager
        .beginTransaction()
        .replace(R.id.main_fragment_container, fragment, LoginFragment.TAG)
        .commit();
  }

  public void showSignupFragment() {
    FragmentManager fragmentManager = getSupportFragmentManager();
    SignupFragment fragment = SignupFragment.getInstance(fragmentManager);

    fragmentManager
        .beginTransaction()
        .replace(R.id.main_fragment_container, fragment, SignupFragment.TAG)
//        .addToBackStack(null)
        .commit();
  }

  public void showChangePasswordFragment() {
    FragmentManager fragmentManager = getSupportFragmentManager();
    ChangePasswordFragment fragment = ChangePasswordFragment.getInstance(fragmentManager);

    fragmentManager
        .beginTransaction()
        .replace(R.id.main_fragment_container, fragment, ChangePasswordFragment.TAG)
//        .addToBackStack(null)
        .commit();
  }

  public void showResendLinkFragment() {
    FragmentManager fragmentManager = getSupportFragmentManager();
    ResendLinkFragment fragment = ResendLinkFragment.getInstance(fragmentManager);

    fragmentManager
        .beginTransaction()
        .replace(R.id.main_fragment_container, fragment, ResendLinkFragment.TAG)
//        .addToBackStack(null)
        .commit();
  }

  private void showWallFragment() {
    FragmentManager fragmentManager = getSupportFragmentManager();
    WallFragment fragment = WallFragment.getInstance(fragmentManager);
    if (!fragment.isAdded()) {
      fragmentManager
          .beginTransaction()
          .replace(R.id.main_fragment_container, fragment, WallFragment.TAG)
          .commit();

    }
  }

  private void showSavedFragment(Bundle savedInstanceState) {
    FragmentManager fragmentManager = getSupportFragmentManager();
    Fragment fragment = fragmentManager.getFragment(savedInstanceState, SAVED_FRAGMENT_KEY);
    fragmentManager
        .beginTransaction()
        .replace(R.id.main_fragment_container, fragment)
        .commit();

    Log.d("MAIN", "Fragment reused.");
  }

  public void logInAs(String email) {
    SharedPreferences sharedPreferences = this.getSharedPreferences(LOGIN_PREFERENCES, MODE_PRIVATE);
    sharedPreferences.edit()
        .putString(EMAIL_KEY, email)
        .apply();
    setIsLoggedIn(true);

    // See comment at the top of NotificationsService class.
    startService(new Intent(this, NotificationsService.class));
  }

  private void logOut() {
    FragmentManager fragmentManager = getSupportFragmentManager();
    WallFragment wallFragment = (WallFragment) fragmentManager.findFragmentByTag(WallFragment.TAG);
    if (wallFragment != null) {
      wallFragment.handleLogOut();
    }

    setIsLoggedIn(false);
  }

  private void setIsLoggedIn(boolean isLoggedIn) {
    synchronized (this) {
      // Destroy every fragment so it's not reused by accident.
      while (getFragmentManager().getBackStackEntryCount() > 0) {
        getFragmentManager().popBackStackImmediate();
      }
      while (getSupportFragmentManager().getBackStackEntryCount() > 0) {
        getSupportFragmentManager().popBackStackImmediate();
      }

      SharedPreferences sharedPreferences = this.getSharedPreferences(LOGIN_PREFERENCES, MODE_PRIVATE);
      sharedPreferences.edit()
          .putBoolean(IS_LOGGED_IN_KEY, isLoggedIn)
          .apply();

      if (isLoggedIn) {
        showWallFragment();
      } else {
        showLoginFragment();
      }

      updateNavigationView();
    }
  }

  private boolean isLoggedIn() {
    SharedPreferences sharedPreferences = this.getSharedPreferences(LOGIN_PREFERENCES, MODE_PRIVATE);
    return sharedPreferences.getBoolean(IS_LOGGED_IN_KEY, false);
  }

  @Override
  public void onBackPressed() {
    DrawerLayout drawer = findViewById(R.id.drawer_layout);
    if (drawer.isDrawerOpen(GravityCompat.START)) {
      drawer.closeDrawer(GravityCompat.START);
    } else {
      if (isLoggedIn()) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(WallFragment.TAG);
        if (fragment == null || !fragment.isAdded()) {
          showWallFragment();
        } else {
          super.onBackPressed();
        }
      } else {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(LoginFragment.TAG);
        if (fragment == null || !fragment.isAdded()) {
          showLoginFragment();
        } else {
          super.onBackPressed();
        }
      }
//      if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
//        super.onBackPressed();
//      } else {
//        for (int i = 0; i < getSupportFragmentManager().getBackStackEntryCount(); ++i) {
//          getSupportFragmentManager().popBackStack();
//        }
//      }
    }
  }

  @Override
  public boolean onNavigationItemSelected(@NonNull MenuItem item) {
    // Handle navigation view item clicks here.
    int id = item.getItemId();

    switch (id) {
      case R.id.nav_show_wall:
        showWallFragment();
        break;
      case R.id.nav_my_sessions:
        showMySessionsFragment(MY_SESSIONS);
        break;
      case R.id.nav_saved_sessions:
        showMySessionsFragment(SAVED_SESSIONS);
        break;
      case R.id.nav_sessions_archive:
        showMySessionsFragment(ARCHIVE_SESSIONS);
        break;
      case R.id.nav_my_account:
        FragmentManager fragmentManager = getSupportFragmentManager();
        MyAccountFragment fragment = MyAccountFragment.getInstance(fragmentManager);
        if (!fragment.isAdded()) {
          fragmentManager
              .beginTransaction()
              .replace(R.id.main_fragment_container, fragment, MyAccountFragment.TAG)
              .commit();
        }
        break;
    }

    DrawerLayout drawer = findViewById(R.id.drawer_layout);
    drawer.closeDrawer(GravityCompat.START);
    return true;
  }

  private void updateNavigationView() {
    NavigationView navigationView = findViewById(R.id.nav_view);
    navigationView.setNavigationItemSelectedListener(this);

    View headerView = navigationView.getHeaderView(0);
    TextView mNameView = headerView.findViewById(R.id.nameView);
    Button logoutButton = headerView.findViewById(R.id.logoutButton);

    logoutButton.setOnClickListener((View view) -> logOut());

    if (isLoggedIn()) {
      SharedPreferences sharedPreferences = this.getSharedPreferences(LOGIN_PREFERENCES, MODE_PRIVATE);
      String email = sharedPreferences.getString(EMAIL_KEY, "");

      mNameView.setText(email);
      setDrawerEnabled(true);
    } else {
      setDrawerEnabled(false);
    }
  }

  public void toggleDrawer() {
    DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
    if (drawerLayout != null) {
      if (drawerLayout.isEnabled()) {
        drawerLayout.openDrawer(Gravity.START);
      }
    }
  }

  private void setDrawerEnabled(boolean enabled) {
    DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
    if (drawerLayout != null) {
      if (enabled) {
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
      } else {
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
      }
    }
  }

  public String getUsersEmail() {
    SharedPreferences sharedPreferences = this.getSharedPreferences(LOGIN_PREFERENCES, MODE_PRIVATE);
    if (isLoggedIn()) {
      return sharedPreferences.getString(EMAIL_KEY, "");
    } else {
      return "";
    }
  }

  private void showMySessionsFragment(String mySessionsType) {
    FragmentManager fragmentManager = getSupportFragmentManager();

    MySessionsFragment fragment = MySessionsFragment.getInstance(fragmentManager, mySessionsType);

    if (!fragment.isAdded()) {
      fragmentManager
          .beginTransaction()
          .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
//          .addToBackStack(null)
          .replace(R.id.main_fragment_container, fragment, MySessionsFragment.TAG)
          .commit();
    }
  }
}

