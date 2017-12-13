package me.pavelgeorgiev.songle

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.support.v7.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.mikepenz.materialdrawer.AccountHeaderBuilder
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.model.DividerDrawerItem
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.mikepenz.materialdrawer.model.ProfileDrawerItem
import me.pavelgeorgiev.songle.Activities.LoginActivity
import me.pavelgeorgiev.songle.Activities.LogoutActivity

/**
 * This class contains common functions used across activities
 */
class CommonFunctions {
    companion object {
        /**
         * Sings out the current user
         */
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

        /**
         * Builds drawer navigation
         *
         * @param items items to be put in the navigation
         * @param context context
         * @param toolbar toolbar if one exists - used to place navigation icon
         *
         * @return DrawerBuilder from which the navigation can be built
         */
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
                    mDrawerBuilder.withToolbar(toolbar)
                }

            return mDrawerBuilder
            }

        /**
         * Converts dp to pixels
         *
         * @param dp value of dp to convert
         * @param context context
         *
         * @return pixel equivalent of specified dp on the device
         */
        fun dpToPx(dp: Int, context: Context): Int {
            val density = context.resources.displayMetrics.density
            return Math.round(dp.toFloat() * density)
        }

        /**
         * Sets an alpha channel of specified color
         * @param yourColor integer representing color resource
         * @param alpha value of alpha channel to be set to the color
         *
         * @return Color with added alpha channel
         */
        fun getColorWithAlpha(yourColor: Int, alpha: Int): Int {
            val red = Color.red(yourColor)
            val blue = Color.blue(yourColor)
            val green = Color.green(yourColor)
            return Color.argb(alpha, red, green, blue)
        }
        }
}