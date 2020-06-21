package com.michael.aadproject;


import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.test.espresso.ViewInteraction;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.pressImeActionButton;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class EspressoLoginTest {

    @Rule
    public ActivityTestRule<SplashActivity> mActivityTestRule = new ActivityTestRule<>(SplashActivity.class);

    @Test
    public void espressoLoginTest() {
        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction appCompatEditText = onView(
                allOf(withId(R.id.editTextLoginEmail),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.ScrollView")),
                                        0),
                                2)));
        appCompatEditText.perform(scrollTo(), replaceText("test@test.com"), closeSoftKeyboard());

        ViewInteraction appCompatEditText2 = onView(
                allOf(withId(R.id.editTextLoginEmail), withText("test@test.com"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.ScrollView")),
                                        0),
                                2)));
        appCompatEditText2.perform(pressImeActionButton());

        ViewInteraction appCompatEditText3 = onView(
                allOf(withId(R.id.editTextLoginPassword),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.ScrollView")),
                                        0),
                                4)));
        appCompatEditText3.perform(scrollTo(), replaceText("logintest"), closeSoftKeyboard());

        ViewInteraction appCompatEditText4 = onView(
                allOf(withId(R.id.editTextLoginPassword), withText("logintest"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.ScrollView")),
                                        0),
                                4)));
        appCompatEditText4.perform(pressImeActionButton());

        ViewInteraction appCompatButton = onView(
                allOf(withId(R.id.buttonLogin), withText("Log In"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.ScrollView")),
                                        0),
                                5)));
        appCompatButton.perform(scrollTo(), click());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(7000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction textView = onView(
                allOf(withId(R.id.textViewWelcome), withText("Welcome back, Michael!"),
                        childAtPosition(
                                childAtPosition(
                                        IsInstanceOf.<View>instanceOf(android.widget.ScrollView.class),
                                        0),
                                0),
                        isDisplayed()));
        textView.check(matches(withText("Welcome back, Michael!")));

        ViewInteraction appCompatImageButton = onView(
                allOf(withContentDescription("Open Sidebar"),
                        childAtPosition(
                                allOf(withId(R.id.toolbar),
                                        childAtPosition(
                                                withClassName(is("android.widget.RelativeLayout")),
                                                0)),
                                1),
                        isDisplayed()));
        appCompatImageButton.perform(click());

        ViewInteraction textView2 = onView(
                allOf(withId(R.id.textProfilePicture), withText("M"),
                        childAtPosition(
                                allOf(withId(R.id.relativeProfile),
                                        childAtPosition(
                                                withId(R.id.sidebarHeader),
                                                0)),
                                1),
                        isDisplayed()));
        textView2.check(matches(withText("M")));

        ViewInteraction textView3 = onView(
                allOf(withId(R.id.textViewProfileName), withText("Michael Michael"),
                        childAtPosition(
                                allOf(withId(R.id.linearProfile),
                                        childAtPosition(
                                                withId(R.id.sidebarHeader),
                                                1)),
                                0),
                        isDisplayed()));
        textView3.check(matches(withText("Michael Michael")));

        ViewInteraction textView4 = onView(
                allOf(withId(R.id.textViewProfileEmail), withText("test@test.com"),
                        childAtPosition(
                                allOf(withId(R.id.linearProfile),
                                        childAtPosition(
                                                withId(R.id.sidebarHeader),
                                                1)),
                                1),
                        isDisplayed()));
        textView4.check(matches(withText("test@test.com")));

        ViewInteraction navigationMenuItemView = onView(
                allOf(childAtPosition(
                        allOf(withId(R.id.design_navigation_view),
                                childAtPosition(
                                        withId(R.id.navigationView),
                                        0)),
                        3),
                        isDisplayed()));
        navigationMenuItemView.perform(click());

        ViewInteraction textView6 = onView(
                allOf(withId(R.id.textViewTitleLogin), withText("SIGN IN"),
                        childAtPosition(
                                childAtPosition(
                                        IsInstanceOf.<View>instanceOf(android.widget.ScrollView.class),
                                        0),
                                0),
                        isDisplayed()));
        textView6.check(matches(withText("SIGN IN")));
    }

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }
}
