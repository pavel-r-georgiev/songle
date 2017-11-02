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


                val user_management = PrimaryDrawerItem().
                        withIdentifier(1)
                        .withSelectable(false)

                val user = FirebaseAuth.getInstance().currentUser

                val header = AccountHeaderBuilder()
                        .withActivity(context as Activity)
                        .withHeaderBackground(R.drawable.header)
                        .addProfiles(ProfileDrawerItem().withEmail(user?.email).withIcon(R.drawable.ic_account_circle_black_24dp))
                        .withSelectionListEnabledForSingleProfile(false)
                        .withProfileImagesVisible(false)



            if (user?.email.isNullOrEmpty()) {
                    user_management.withName("Account Management")
                    user_management.withIcon(R.drawable.ic_account_circle_black_24dp)
                    user_management.withOnDrawerItemClickListener(Drawer.OnDrawerItemClickListener({ _, _, _ ->
                        context.startActivity(Intent(context, LogoutActivity::class.java))
                        return@OnDrawerItemClickListener true
                    }))
                } else {
                    header.withOnAccountHeaderSelectionViewClickListener({ _, _ ->
                        context.startActivity(Intent(context, LogoutActivity::class.java))
                        return@withOnAccountHeaderSelectionViewClickListener true
                    })
                    user_management.withName("Sign out")
                    user_management.withIcon(R.drawable.ic_exit_to_app_black_24dp)
                    user_management.withOnDrawerItemClickListener(Drawer.OnDrawerItemClickListener({ _, _, _ ->
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
                                user_management
                        )
                        .withFullscreen(true)


                if(toolbar != null){
                    mDrawerBuilder.withToolbar(toolbar!!)
                }

            return mDrawerBuilder
            }
        }
}