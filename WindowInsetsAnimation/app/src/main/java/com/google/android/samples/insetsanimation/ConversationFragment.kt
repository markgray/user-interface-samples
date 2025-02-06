/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.samples.insetsanimation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsetsAnimation
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsAnimationCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.samples.insetsanimation.databinding.FragmentConversationBinding

/**
 * The main entry point for the sample. See [onViewCreated] for more information on how
 * the sample works.
 */
class ConversationFragment : Fragment() {
    /**
     * The "backing" field for our read only [FragmentConversationBinding] field [binding]. It exists
     * apparently only to avoid having to use !! every time we reference [binding]. It is initialize
     * in our [onCreateView] override by using the [FragmentConversationBinding.inflate] method to
     * inflate its layout file layout/fragment_conversation.xml into a binding object.
     */
    private var _binding: FragmentConversationBinding? = null

    /**
     * Read only access to our null-able [FragmentConversationBinding] field [_binding].
     */
    private val binding: FragmentConversationBinding get() = _binding!!

    /**
     * Called to have the fragment instantiate its user interface view. This will be called between
     * [onCreate] and [onViewCreated]. It is recommended to **only** inflate the layout in this method
     * and move logic that operates on the returned View to [onViewCreated]. If you return a [View]
     * from here, you will later be called in [onDestroyView] when the view is being released. We
     * initialize our [FragmentConversationBinding] field [_binding] to the binding object that the
     * [FragmentConversationBinding.inflate] method inflates from its associated layout file
     * layout/fragment_conversation.xml using our parameter [inflater] as the [LayoutInflater], and
     * our [ViewGroup] parameter [container] for the `LayoutParams` without attaching to the view.
     * Then we return the outermost [View] in the associated layout file to the caller.
     *
     * @param inflater The [LayoutInflater] object that can be used to inflate
     * any views in the fragment,
     * @param container If non-`null`, this is the parent view that the fragment's
     * UI will be attached to. The fragment should not add the view itself,
     * but this can be used to generate the `LayoutParams` of the view.
     * @param savedInstanceState If non-`null`, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return Return the [View] for the fragment's UI, or `null`.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConversationBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Called immediately after [onCreateView] has returned, but before any saved state has been
     * restored in to the view. This gives subclasses a chance to initialize themselves once they
     * know their view hierarchy has been completely created. First we set the `adapter` of the
     * [RecyclerView] in the [binding] to our layout file with ID `R.id.conversation_recyclerview`
     * (ie. the [FragmentConversationBinding.conversationRecyclerview] property of [binding]) to a
     * new instance of [ConversationAdapter]. The rest of the override sets our activity up to use
     * [WindowInsetsAnimation] to animate its views and the ime, and is well documented in the code.
     *
     * @param view The [View] returned by [onCreateView].
     * @param savedInstanceState If non-`null`, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Set our conversation adapter on the RecyclerView
        binding.conversationRecyclerview.adapter = ConversationAdapter()

        // There are three steps to WindowInsetsAnimations:

        /**
         * 1) Since our Activity has declared `window.setDecorFitsSystemWindows(false)`, we need to
         * handle any [WindowInsetsCompat] as appropriate.
         *
         * Our [RootViewDeferringInsetsCallback] will update our attached view's padding to match
         * the combination of the [WindowInsetsCompat.Type.systemBars], and selectively apply the
         * [WindowInsetsCompat.Type.ime] insets, depending on any ongoing WindowInsetAnimations
         * (see that class for more information).
         */
        val deferringInsetsListener = RootViewDeferringInsetsCallback(
            persistentInsetTypes = WindowInsetsCompat.Type.systemBars(),
            deferredInsetTypes = WindowInsetsCompat.Type.ime()
        )
        // RootViewDeferringInsetsCallback is both an WindowInsetsAnimation.Callback and an
        // OnApplyWindowInsetsListener, so needs to be set as so.
        ViewCompat.setWindowInsetsAnimationCallback(binding.root, deferringInsetsListener)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root, deferringInsetsListener)

        /**
         * 2) The second step is reacting to any animations which run. This can be system driven,
         * such as the user focusing on an EditText and on-screen keyboard (IME) coming on screen,
         * or app driven (more on that in step 3).
         *
         * To react to animations, we set an [android.view.WindowInsetsAnimation.Callback] on any
         * views which we wish to react to inset animations. In this example, we want our
         * EditText holder view, and the conversation RecyclerView to react.
         *
         * We use our [TranslateDeferringInsetsAnimationCallback] class, bundled in this sample,
         * which will automatically move each view as the IME animates.
         *
         * Note about [TranslateDeferringInsetsAnimationCallback], it relies on the behavior of
         * [RootViewDeferringInsetsCallback] on the layout's root view.
         */
        ViewCompat.setWindowInsetsAnimationCallback(
            binding.messageHolder,
            TranslateDeferringInsetsAnimationCallback(
                view = binding.messageHolder,
                persistentInsetTypes = WindowInsetsCompat.Type.systemBars(),
                deferredInsetTypes = WindowInsetsCompat.Type.ime(),
                // We explicitly allow dispatch to continue down to binding.messageHolder's
                // child views, so that step 2.5 below receives the call
                dispatchMode = WindowInsetsAnimationCompat.Callback.DISPATCH_MODE_CONTINUE_ON_SUBTREE
            )
        )
        ViewCompat.setWindowInsetsAnimationCallback(
            binding.conversationRecyclerview,
            TranslateDeferringInsetsAnimationCallback(
                view = binding.conversationRecyclerview,
                persistentInsetTypes = WindowInsetsCompat.Type.systemBars(),
                deferredInsetTypes = WindowInsetsCompat.Type.ime()
            )
        )

        /**
         * 2.5) We also want to make sure that our EditText is focused once the IME
         * is animated in, to enable it to accept input. Similarly, if the IME is animated
         * off screen and the EditText is focused, we should clear that focus.
         *
         * The bundled [ControlFocusInsetsAnimationCallback] callback will automatically request
         * and clear focus for us.
         *
         * Since `binding.messageEdittext` is a child of `binding.messageHolder`, this
         * [WindowInsetsAnimationCompat.Callback] will only work if the ancestor view's callback uses the
         * [WindowInsetsAnimationCompat.Callback.DISPATCH_MODE_CONTINUE_ON_SUBTREE] dispatch mode, which
         * we have done above.
         */
        ViewCompat.setWindowInsetsAnimationCallback(
            binding.messageEdittext,
            ControlFocusInsetsAnimationCallback(binding.messageEdittext)
        )

        /**
         * 3) The third step is when the app wants to control and drive an inset animation.
         * This is an optional step, but suits many types of input UIs. The example scenario we
         * use in this sample is that the user can drag open the IME, by over-scrolling the
         * conversation RecyclerView. To enable this, we use a [InsetsAnimationLinearLayout] as a
         * root view in our layout which handles this automatically for scrolling views,
         * through nested scrolling.
         *
         * Alternatively, this sample also contains [InsetsAnimationTouchListener],
         * which is a [android.view.View.OnTouchListener] which does similar for non-scrolling
         * views, detecting raw drag events rather than scroll events to open/close the IME.
         *
         * Internally, both [InsetsAnimationLinearLayout] & [InsetsAnimationTouchListener] use a
         * class bundled in this sample called [SimpleImeAnimationController], which simplifies
         * much of the mechanics for controlling a [WindowInsetsAnimationCompat].
         */
    }

    /**
     * Called when the view previously created by [onCreateView] has been detached from the fragment.
     * The next time the fragment needs to be displayed, a new view will be created. This is called
     * after [onStop] and before [onDestroy].  It is called regardless of whether [onCreateView]
     * returned a non-`null` view. Internally it is called after the view's state has been saved but
     * before it has been removed from its parent. We call our super's implementation of `onDestroyView`
     * then set our [FragmentConversationBinding] field [_binding] to `null` (our [onCreateView]
     * override will recreate it for us if we are re-started).
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
