package satd.step2

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue


class JavaMethodTest {

    @Test
    fun `has inner methods`() {
        assertTrue {  JavaMethod(src1).hasInnerMethods }
    }


    @Test
    fun `no inner methods`() {
        assertFalse {  JavaMethod(src2).hasInnerMethods }
    }

    @Test
    fun `no inner methods but anonymous object`() {
        assertFalse {  JavaMethod(src3).hasInnerMethods }
    }

    @Test
    fun `no inner methods because abstract method`() {
        assertFalse {  JavaMethod(src4).hasInnerMethods }
    }


}

const val src1 = """

    double method3() {
        int offset = 10;
        pippo();
         gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Gallery gallery = (Gallery) adapter.getItem(position);
                String status = preferences.getString(gallery.getGalleryName(), "");
                Intent galleryImagesIntent = new Intent(getActivity(), LocalImageActivity.class);
                galleryImagesIntent.putExtra("--##string##--", gallery.getGalleryPath());
                startActivity(galleryImagesIntent);
            }
            
        });
        return java.lang.Math.random() + offset;
    }

"""
const val src0 = """
 
void loadLocalGallery() {
    ArrayList<Gallery> imageFolders = new ArrayList<Gallery>();
    imageFolders = new ArrayList<Gallery>(new LinkedHashSet<Gallery>(folders));
    adapter = new GalleryListAdapter(getActivity(), imageFolders);
    gridView.setAdapter(adapter);
    gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Gallery gallery = (Gallery) adapter.getItem(position);
            String status = preferences.getString(gallery.getGalleryName(), "");
            Intent galleryImagesIntent = new Intent(getActivity(), LocalImageActivity.class);
            galleryImagesIntent.putExtra("--##string##--", gallery.getGalleryPath());
            startActivity(galleryImagesIntent);
        }
    });
}

"""
const val src2 = """
 
void loadLocalGallery() {
    ArrayList<Gallery> imageFolders = new ArrayList<Gallery>();
    imageFolders = new ArrayList<Gallery>(new LinkedHashSet<Gallery>(folders));
    adapter = new GalleryListAdapter(getActivity(), imageFolders);
    gridView.setAdapter(adapter);
}

"""
const val src3 = """
 
public IElementSemantics getSemantics(ISemanticContext br) {
    return new IElementSemantics() {
    };
}

"""


const val src4 = """@Override
public abstract INamedElementSemantics getSemantics();"""