Android Slice Viewer
=====================

This application allows testing the embedding behavior of Android Slices inside
other applications.

There are four slices available (as far as I am currently able to discern):

 - content://com.example.android.sliceviewer/hello
 - content://com.example.android.sliceviewer/test
 - https://sliceviewer.android.example.com/hello
 - https://sliceviewer.android.example.com/test

Will add more as I analyze the source code.

(A whole lot of typing to get right in the search view in my opinion)

The starting Activity is .ui.list.SliceViewerActivity, and its UI consists of a LinearLayout root
view, which holds a FrameLayout which contains a CardView holding a Toolbar holding a SearchView.
Below this in the LinearLayout is a RecyclerView which holds view holders for each of the slices
that have been searched for in the SearchView. Each of these slices can be swiped to remove them
from the RecyclerView, and they are preserved in the SharedPreferences of the app by the CRUD local
data source LocalUriDataSource.

