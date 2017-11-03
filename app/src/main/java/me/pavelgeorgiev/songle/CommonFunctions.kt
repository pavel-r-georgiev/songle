package me.pavelgeorgiev.songle

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v7.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.mikepenz.materialdrawer.AccountHeaderBuilder
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.model.DividerDrawerItem
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.mikepenz.materialdrawer.model.ProfileDrawerItem

class CommonFunctions {
    companion object {
        fun signOut(context: Context){
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(context, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK or
                        Intent.FLAG_ACTIVITY_NEW_TASK

                val activity = context as Activity
                activity.startActivity(intent)
                activity.finish()
            }

        fun buildDrawerNav(items: Array<PrimaryDrawerItem>, context: Context, toolbar: Toolbar? = null): DrawerBuilder {
                val item1 = PrimaryDrawerItem().
                        withIdentifier(1)
                        .withName("Completed Songs")
                        .withIcon(R.drawable.ic_library_music_black_24dp)
                        .withSelectable(false)


                val userManagement = PrimaryDrawerItem().
                        withIdentifier(1)
                        .withSelectable(false)

                val user = FirebaseAuth.getInstance().currentUser

                val userProfile = ProfileDrawerItem().withEmail(user?.email ?: "Anonymous")

                if(user?.email.isNullOrEmpty()){
                    userProfile.withEmail("Anonymous")
                } else {
                    userProfile.withEmail(user?.email)
                }

                val header = AccountHeaderBuilder()
                        .withActivity(context as Activity)
                        .withHeaderBackground(R.drawable.header)
                        .addProfiles(userProfile)
                        .withSelectionListEnabledForSingleProfile(false)
                        .withProfileImagesVisible(false)

            if (user?.email.isNullOrEmpty()) {
                    userManagement.withName("Account Management")
                    userManagement.withIcon(R.drawable.ic_account_circle_black_24dp)
                    userManagement.withOnDrawerItemClickListener(Drawer.OnDrawerItemClickListener({ _, _, _ ->
                        context.startActivity(Intent(context, LogoutActivity::class.java))
                        return@OnDrawerItemClickListener true
                    }))
                } else {
                    header.withOnAccountHeaderSelectionViewClickListener({ _, _ ->
                        context.startActivity(Intent(context, LogoutActivity::class.java))
                        return@withOnAccountHeaderSelectionViewClickListener true
                    })
                    userManagement.withName("Sign out")
                    userManagement.withIcon(R.drawable.ic_exit_to_app_black_24dp)
                    userManagement.withOnDrawerItemClickListener(Drawer.OnDrawerItemClickListener({ _, _, _ ->
                        CommonFunctions.signOut(context)
                        return@OnDrawerItemClickListener true
                    }))
                }


               val mDrawerBuilder = DrawerBuilder()
                        .withAccountHeader(header.build())
                        .withActivity(context)
                        .addDrawerItems(
                                *items,
                                DividerDrawerItem(),
                                userManagement
                        )
                        .withFullscreen(true)


                if(toolbar != null){
                    mDrawerBuilder.withToolbar(toolbar!!)
                }

            return mDrawerBuilder
            }
        }
}