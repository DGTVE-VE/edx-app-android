package org.edx.mobile.view;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.widget.TextView;

import org.edx.mobile.R;
import org.edx.mobile.http.OkHttpUtil;
import org.edx.mobile.model.Filter;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.model.course.BlockType;
import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.model.course.DiscussionBlockModel;
import org.edx.mobile.model.course.EncodedVideos;
import org.edx.mobile.model.course.HtmlBlockModel;
import org.edx.mobile.model.course.VideoBlockModel;
import org.edx.mobile.model.course.VideoData;
import org.edx.mobile.model.course.VideoInfo;
import org.edx.mobile.module.prefs.PrefManager;
import org.edx.mobile.view.adapters.CourseUnitPagerAdapter;
import org.edx.mobile.view.custom.DisableableViewPager;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.util.ActivityController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import static org.assertj.android.api.Assertions.assertThat;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

// The SDK version needs to be lesser than Lollipop because of this
// issue: https://github.com/robolectric/robolectric/issues/1810
@Config(sdk = 19)
public class CourseUnitNavigationActivityTest extends CourseBaseActivityTest {
    /**
     * Method for defining the subclass of {@link CourseUnitNavigationActivity}
     * that is being tested. Should be overridden by subclasses.
     *
     * @return The {@link CourseUnitNavigationActivity} subclass that is being
     * tested
     */
    @Override
    protected Class<? extends CourseUnitNavigationActivity> getActivityClass() {
        return CourseUnitNavigationActivity.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Intent getIntent() {
        EnrolledCoursesResponse courseData;
        CourseComponent courseComponent;
        try {
            courseData = api.getEnrolledCourses().get(0);
            courseComponent = serviceManager.getCourseStructure(
                    courseData.getCourse().getId(),
                    OkHttpUtil.REQUEST_CACHE_TYPE.IGNORE_CACHE);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        List<CourseComponent> leafComponents = new ArrayList<>();
        courseComponent.fetchAllLeafComponents(leafComponents,
                EnumSet.allOf(BlockType.class));
        CourseComponent courseUnit = leafComponents.get(0);
        Intent intent = super.getIntent();
        Bundle extras = new Bundle();
        extras.putSerializable(Router.EXTRA_COURSE_DATA, courseData);
        extras.putString(Router.EXTRA_COURSE_COMPONENT_ID, courseUnit.getId());
        intent.putExtra(Router.EXTRA_BUNDLE, extras);

        // Change the settings to show all the course content for easier and
        // more comprehensive testing
        new PrefManager.UserPrefManager(RuntimeEnvironment.application)
                .setUserPrefVideoModel(false);
        return intent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean appliesPrevTransitionOnRestart() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Test
    @Override
    public void initializeTest() {
        super.initializeTest();

        Intent intent = getIntent();
        ActivityController<? extends CourseUnitNavigationActivity> controller =
                Robolectric.buildActivity(getActivityClass()).withIntent(intent);
        CourseUnitNavigationActivity activity = controller.get();

        controller.create();
        assertNotNull(activity.findViewById(R.id.course_unit_nav_bar));
        View prev = activity.findViewById(R.id.goto_prev);
        assertNotNull(prev);
        assertThat(prev).isInstanceOf(TextView.class);
        TextView prevButton = (TextView) prev;
        View next = activity.findViewById(R.id.goto_next);
        assertNotNull(next);
        assertThat(next).isInstanceOf(TextView.class);
        TextView nextButton = (TextView) next;
        View prevUnitTitle = activity.findViewById(R.id.prev_unit_title);
        assertNotNull(prevUnitTitle);
        assertThat(prevUnitTitle).isInstanceOf(TextView.class);
        TextView prevUnitLabel = (TextView) prevUnitTitle;
        View nextUnitTitle = activity.findViewById(R.id.next_unit_title);
        assertNotNull(nextUnitTitle);
        assertThat(nextUnitTitle).isInstanceOf(TextView.class);
        TextView nextUnitLabel = (TextView) nextUnitTitle;
        View pager = activity.findViewById(R.id.pager);
        assertNotNull(pager);
        assertThat(pager).isInstanceOf(ViewPager.class);
        ViewPager viewPager = (ViewPager) pager;
        PagerAdapter pagerAdapter = viewPager.getAdapter();
        assertNotNull(pagerAdapter);

        // Text navigation through units
        Bundle extras = intent.getBundleExtra(Router.EXTRA_BUNDLE);
        EnrolledCoursesResponse courseData = (EnrolledCoursesResponse)
                extras.getSerializable(Router.EXTRA_COURSE_DATA);
        assertNotNull(courseData);
        CourseComponent courseComponent;
        try {
            courseComponent = serviceManager.getCourseStructure(
                    courseData.getCourse().getId(),
                    OkHttpUtil.REQUEST_CACHE_TYPE.IGNORE_CACHE);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        assertNotNull(courseComponent);
        final String unitId = extras.getString(Router.EXTRA_COURSE_COMPONENT_ID);
        assertNotNull(unitId);
        CourseComponent currentUnit = courseComponent.find(new Filter<CourseComponent>() {
            @Override
            public boolean apply(CourseComponent courseComponent) {
                return unitId.equals(courseComponent.getId());
            }
        });
        assertNotNull(currentUnit);
        // Since Robolectric current does not call the scroll callbacks due
        // to not supporting view drawing (see
        // https://github.com/robolectric/robolectric/issues/2007), we can't
        // test the ViewPager navigation at the moment.
        // TODO: Uncomment the following code when this issue is fixed
        /*List<CourseComponent> units = new ArrayList<>();
        courseComponent.fetchAllLeafComponents(units,
                EnumSet.allOf(BlockType.class));
        assertThat(units).isNotEmpty();
        controller.start().postCreate((Bundle)null).resume().visible();
        ListIterator<CourseComponent> unitIterator = units.listIterator(1);
        for (CourseComponent prevUnit = null;;) {
            int unitIndex = unitIterator.previousIndex();
            CourseComponent nextUnit = unitIterator.hasNext() ?
                    unitIterator.next() : null;
            verifyState(activity, unitIndex, currentUnit, prevUnit, nextUnit,
                    viewPager, pagerAdapter, prevButton, nextButton,
                    prevUnitLabel, nextUnitLabel);
            if (nextUnit == null) break;
            // The Scheduler needs to be paused while clicking the next button
            // to enable the FragmentStatePagerAdapter to clear it's transaction
            // state, and thus avoid the commit being called recursively
            Scheduler foregroundScheduler = ShadowApplication.getInstance()
                    .getForegroundThreadScheduler();
            foregroundScheduler.pause();
            assertTrue(nextButton.performClick());
            foregroundScheduler.unPause();
            prevUnit = currentUnit;
            currentUnit = nextUnit;
        }
        // Now iterate back in reverse order to test the previous button
        unitIterator = units.listIterator(units.size() - 1);
        for (CourseComponent nextUnit = null;;) {
            int unitIndex = unitIterator.nextIndex();
            CourseComponent prevUnit = unitIterator.hasPrevious() ?
                    unitIterator.previous() : null;
            verifyState(activity, unitIndex, currentUnit, prevUnit, nextUnit,
                    viewPager, pagerAdapter, prevButton, nextButton,
                    prevUnitLabel, nextUnitLabel);
            if (prevUnit == null) break;
            Scheduler foregroundScheduler = ShadowApplication.getInstance()
                    .getForegroundThreadScheduler();
            foregroundScheduler.pause();
            assertTrue(prevButton.performClick());
            foregroundScheduler.unPause();
            nextUnit = currentUnit;
            currentUnit = prevUnit;
        }*/
    }

    /**
     * Testing download progress menu visibility states and click behaviour
     * (starting DownloadActivity). Only when both AppConstants.offline_flag
     * is true and there is a downloading entry in the database, should the
     * progress bar be visible.
     */
    /**
     * Generic method for verifying the state of the CourseUnitNavigationActivity
     * at a specific unit.
     *
     * @param activity An instance of CourseUnitNavigationActivity that has been
     *                 initialized
     * @param unitIndex The index of the unit among all the leaves for the course
     * @param currentUnit The selected course unit
     * @param prevUnit The unit previous to the current selection
     * @param nextUnit The unit next to the current selection
     * @param viewPager The ViewPager instance containing the CourseUnitFragment
     *                  instances
     * @param pagerAdapter The PagerAdapter associated with the ViewPager
     * @param prevButton The button for going to the previous unit
     * @param nextButton The button for going to the next unit
     * @param prevUnitLabel The label for the previous unit
     * @param nextUnitLabel The label for the next unit
     */
    private void verifyState(CourseUnitNavigationActivity activity,
            int unitIndex, CourseComponent currentUnit,
            CourseComponent prevUnit, CourseComponent nextUnit,
            ViewPager viewPager, PagerAdapter pagerAdapter,
            TextView prevButton, TextView nextButton,
            TextView prevUnitLabel, TextView nextUnitLabel) {
        assertTitle(activity, currentUnit.getDisplayName());

        Class<? extends CourseUnitFragment> fragmentClass;
        if (currentUnit instanceof VideoBlockModel) {
            fragmentClass = CourseUnitVideoFragment.class;
        } else if (!currentUnit.isMultiDevice() ){
            fragmentClass = CourseUnitMobileNotSupportedFragment.class;
        } else if (currentUnit.getType() != BlockType.VIDEO &&
                currentUnit.getType() != BlockType.HTML &&
                currentUnit.getType() != BlockType.OTHERS &&
                currentUnit.getType() != BlockType.DISCUSSION &&
                currentUnit.getType() != BlockType.PROBLEM ) {
            fragmentClass = CourseUnitEmptyFragment.class;
        } else if (currentUnit instanceof HtmlBlockModel) {
            fragmentClass = CourseUnitWebViewFragment.class;
        } else {
            fragmentClass = CourseUnitMobileNotSupportedFragment.class;
        }
        Object item = pagerAdapter.instantiateItem(viewPager, unitIndex);
        assertNotNull(item);
        assertThat(item).isInstanceOf(fragmentClass);
        Bundle args = ((Fragment) item).getArguments();
        assertNotNull(args);
        assertEquals(currentUnit, args.getSerializable(
                Router.EXTRA_COURSE_UNIT));

        assertEquals(prevUnit != null, prevButton.isEnabled());
        assertEquals(nextUnit != null, nextButton.isEnabled());
        CourseComponent prevSection = prevUnit == null ?
                null : prevUnit.getParent();
        CourseComponent nextSection = nextUnit == null ?
                null : nextUnit.getParent();
        if (prevSection == null ||
                currentUnit.getParent().equals(prevSection)) {
            assertThat(prevUnitLabel).isNotVisible();
            assertThat(prevButton).hasText(R.string.assessment_previous);
        } else {
            assertThat(prevUnitLabel).isVisible();
            assertThat(prevUnitLabel).hasText(prevSection.getDisplayName());
            assertThat(prevButton).hasText(R.string.assessment_previous_unit);
        }
        if (nextSection == null ||
                currentUnit.getParent().equals(nextSection)) {
            assertThat(nextUnitLabel).isNotVisible();
            assertThat(nextButton).hasText(R.string.assessment_next);
        } else {
            assertThat(nextUnitLabel).isVisible();
            assertThat(nextUnitLabel).hasText(nextSection.getDisplayName());
            assertThat(nextButton).hasText(R.string.assessment_next_unit);
        }
    }
    /**
     * Generic method for asserting proper setup for the current orientation
     *
     * @param activity The current activity
     */
    private void assertOrientationSetup(CourseUnitNavigationActivity activity) {
        boolean isLandscape = activity.getResources().getConfiguration().orientation ==
                Configuration.ORIENTATION_LANDSCAPE;
        ActionBar bar = activity.getSupportActionBar();
        if (bar != null) {
            assertEquals(!isLandscape, bar.isShowing());
        }
        View courseUnitNavBar = activity.findViewById(R.id.course_unit_nav_bar);
        assertNotNull(courseUnitNavBar);
        if (isLandscape) {
            assertThat(courseUnitNavBar).isNotVisible();
        } else {
            assertThat(courseUnitNavBar).isVisible();
        }
        View pagerView = activity.findViewById(R.id.pager);
        assertNotNull(pagerView);
        assertThat(pagerView).isInstanceOf(DisableableViewPager.class);
        assertEquals(!isLandscape, ((DisableableViewPager) pagerView).isEnabled());
    }

    /**
     * Generic method for testing the setup for the specified orientation
     *
     * @param orientation The orientation to be tested
     */
    public void testOrientationSetup(int orientation) {
        ActivityController<? extends CourseUnitNavigationActivity> controller =
                Robolectric.buildActivity(getActivityClass()).withIntent(getIntent());
        CourseUnitNavigationActivity activity = controller.get();
        activity.getResources().getConfiguration().orientation = orientation;
        controller.create();
        assertOrientationSetup(activity);
    }

    /**
     * Testing setup for different orientations
     */
    @Test
    public void orientationsSetupTest() {
        testOrientationSetup(Configuration.ORIENTATION_PORTRAIT);
        testOrientationSetup(Configuration.ORIENTATION_LANDSCAPE);
    }

    /**
     * Generic method for testing setup on orientation changes
     *
     * @param activity The current activity
     * @param orientation The orientation change to test
     */
    private void testOrientationChange(
            CourseUnitNavigationActivity activity, int orientation) {
        Configuration config = activity.getResources().getConfiguration();
        assertNotEquals(orientation, config.orientation);
        config.orientation = orientation;
        activity.onConfigurationChanged(config);
        assertOrientationSetup(activity);
    }

    /**
     * Testing orientation changes
     */
    @Test
    public void orientationChangeTest() {
        CourseUnitNavigationActivity activity =
                Robolectric.buildActivity(getActivityClass())
                        .withIntent(getIntent()).setup().get();
        // Orientation needs to be explicitly set as PORTRAIT since its UNDEFINED initially
        testOrientationChange(activity, Configuration.ORIENTATION_PORTRAIT);
        assertEquals(Configuration.ORIENTATION_PORTRAIT,
                activity.getResources().getConfiguration().orientation);
        assertOrientationSetup(activity);

        testOrientationChange(activity, Configuration.ORIENTATION_LANDSCAPE);
        assertEquals(Configuration.ORIENTATION_LANDSCAPE,
                activity.getResources().getConfiguration().orientation);
        assertOrientationSetup(activity);
    }

    /**
     * Testing creation of various fragments in the {@link CourseUnitNavigationActivity}'s
     * ViewPager, by supplying its {@link CourseUnitPagerAdapter} with all possible
     * {@link CourseComponent} models.
     */
    @Test
    public void testUnitFragmentCreation() {
        FragmentManager fragmentManager = Mockito.mock(FragmentManager.class);
        EnrolledCoursesResponse courseData = Mockito.mock(EnrolledCoursesResponse.class);
        CourseUnitFragment.HasComponent hasComponent = Mockito.mock(CourseUnitFragment.HasComponent.class);

        List<CourseComponent> unitList = new ArrayList<>();

        VideoBlockModel encodeVideosModel = Mockito.mock(VideoBlockModel.class);
        VideoData videoData = Mockito.mock(VideoData.class);
        videoData.encodedVideos = Mockito.mock(EncodedVideos.class);
        when(videoData.encodedVideos.getPreferredVideoInfo())
                .thenReturn(Mockito.mock(VideoInfo.class));
        when(encodeVideosModel.getData()).thenReturn(videoData);
        unitList.add(encodeVideosModel);

        VideoBlockModel youtubeVideosModel = Mockito.mock(VideoBlockModel.class);
        VideoData videoData2 = Mockito.mock(VideoData.class);
        videoData2.encodedVideos = Mockito.mock(EncodedVideos.class);
        when(videoData2.encodedVideos.getYoutubeVideoInfo())
                .thenReturn(Mockito.mock(VideoInfo.class));
        when(youtubeVideosModel.getData()).thenReturn(videoData2);
        unitList.add(youtubeVideosModel);

        DiscussionBlockModel discussionModel = Mockito.mock(DiscussionBlockModel.class);
        unitList.add(discussionModel);

        CourseComponent nonMultiDeviceModel = Mockito.mock(CourseComponent.class);
        when(nonMultiDeviceModel.isMultiDevice()).thenReturn(false);
        unitList.add(nonMultiDeviceModel);

        HtmlBlockModel htmlModel = Mockito.mock(HtmlBlockModel.class);
        when(htmlModel.isMultiDevice()).thenReturn(true);
        when(htmlModel.getType()).thenReturn(BlockType.HTML);
        unitList.add(htmlModel);

        CourseComponent unknownModel = Mockito.mock(CourseComponent.class);
        when(unknownModel.isMultiDevice()).thenReturn(true);
        when(unknownModel.getType()).thenReturn(BlockType.COURSE);
        unitList.add(unknownModel);

        CourseComponent problemModel = Mockito.mock(CourseComponent.class);
        when(problemModel.isMultiDevice()).thenReturn(true);
        when(problemModel.getType()).thenReturn(BlockType.PROBLEM);
        unitList.add(problemModel);

        CourseComponent othersModel = Mockito.mock(CourseComponent.class);
        when(othersModel.isMultiDevice()).thenReturn(true);
        when(othersModel.getType()).thenReturn(BlockType.OTHERS);
        unitList.add(othersModel);

        CourseUnitPagerAdapter adapter = new CourseUnitPagerAdapter(fragmentManager, unitList,
                courseData, hasComponent);

        assertTrue(adapter.getItem(0) instanceof CourseUnitVideoFragment);
        assertTrue(adapter.getItem(1) instanceof CourseUnitOnlyOnYoutubeFragment);
        assertTrue(adapter.getItem(2) instanceof CourseUnitDiscussionFragment);
        assertTrue(adapter.getItem(3) instanceof CourseUnitMobileNotSupportedFragment);
        assertTrue(adapter.getItem(4) instanceof CourseUnitWebViewFragment);
        assertTrue(adapter.getItem(5) instanceof CourseUnitEmptyFragment);
        assertTrue(adapter.getItem(6) instanceof CourseUnitMobileNotSupportedFragment);
        assertTrue(adapter.getItem(7) instanceof CourseUnitMobileNotSupportedFragment);
    }

    // TODO: Robolectric doesn't have a Gradle-based parameterized test suite, need to implement it.
    @Ignore
    @RunWith(Parameterized.class)
    public static class ParameterizedFragmentCreation {
        @Parameterized.Parameters(name= "{index}: testUnitFragmentCreation({0})={1}")
        public static Collection<Object[]> data() {
            List<CourseComponent> unitList = new ArrayList<>();

            VideoBlockModel encodeVideosModel = Mockito.mock(VideoBlockModel.class);
            VideoData videoData = Mockito.mock(VideoData.class);
            videoData.encodedVideos = Mockito.mock(EncodedVideos.class);
            when(videoData.encodedVideos.getPreferredVideoInfo())
                    .thenReturn(Mockito.mock(VideoInfo.class));
            when(encodeVideosModel.getData()).thenReturn(videoData);
            unitList.add(encodeVideosModel);

            VideoBlockModel youtubeVideosModel = Mockito.mock(VideoBlockModel.class);
            VideoData videoData2 = Mockito.mock(VideoData.class);
            videoData2.encodedVideos = Mockito.mock(EncodedVideos.class);
            when(videoData2.encodedVideos.getYoutubeVideoInfo())
                    .thenReturn(Mockito.mock(VideoInfo.class));
            when(youtubeVideosModel.getData()).thenReturn(videoData2);
            unitList.add(youtubeVideosModel);

            DiscussionBlockModel discussionModel = Mockito.mock(DiscussionBlockModel.class);
            unitList.add(discussionModel);

            CourseComponent nonMultiDeviceModel = Mockito.mock(CourseComponent.class);
            when(nonMultiDeviceModel.isMultiDevice()).thenReturn(false);
            unitList.add(nonMultiDeviceModel);

            HtmlBlockModel htmlModel = Mockito.mock(HtmlBlockModel.class);
            when(htmlModel.isMultiDevice()).thenReturn(true);
            when(htmlModel.getType()).thenReturn(BlockType.HTML);
            unitList.add(htmlModel);

            CourseComponent unknownModel = Mockito.mock(CourseComponent.class);
            when(unknownModel.isMultiDevice()).thenReturn(true);
            when(unknownModel.getType()).thenReturn(BlockType.COURSE);
            unitList.add(unknownModel);

            CourseComponent problemModel = Mockito.mock(CourseComponent.class);
            when(problemModel.isMultiDevice()).thenReturn(true);
            when(problemModel.getType()).thenReturn(BlockType.PROBLEM);
            unitList.add(problemModel);

            CourseComponent othersModel = Mockito.mock(CourseComponent.class);
            when(othersModel.isMultiDevice()).thenReturn(true);
            when(othersModel.getType()).thenReturn(BlockType.OTHERS);
            unitList.add(othersModel);

            return Arrays.asList(new Object[][] {
                    {unitList.get(0), CourseUnitVideoFragment.class},
                    {unitList.get(1), CourseUnitOnlyOnYoutubeFragment.class},
                    {unitList.get(2), CourseUnitDiscussionFragment.class},
                    {unitList.get(3), CourseUnitMobileNotSupportedFragment.class},
                    {unitList.get(4), CourseUnitWebViewFragment.class},
                    {unitList.get(5), CourseUnitEmptyFragment.class},
                    {unitList.get(6), CourseUnitMobileNotSupportedFragment.class},
                    {unitList.get(7), CourseUnitMobileNotSupportedFragment.class}
            });
        }

        @Parameterized.Parameter
        public CourseComponent input;

        @Parameterized.Parameter(value = 1)
        public Class expected;

        @Test
        public void test() {
            FragmentManager fragmentManager = Mockito.mock(FragmentManager.class);
            EnrolledCoursesResponse courseData = Mockito.mock(EnrolledCoursesResponse.class);
            CourseUnitFragment.HasComponent hasComponent = Mockito.mock(CourseUnitFragment.HasComponent.class);

            CourseUnitPagerAdapter adapter = new CourseUnitPagerAdapter(fragmentManager,
                    Collections.singletonList(input),
                    courseData, hasComponent);

//            Assert.assertThat(adapter.getItem(0), instanceOf(expected));
            assertThat(adapter.getItem(0)).isInstanceOf(expected);
        }
    }
}
