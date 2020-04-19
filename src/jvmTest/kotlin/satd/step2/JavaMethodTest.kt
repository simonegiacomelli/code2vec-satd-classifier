package satd.step2

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue


class JavaMethodTest {

    @Test
    fun `has inner methods`() {
        assertFalse {  JavaMethod(src1).valid }
    }


    @Test
    fun `no inner methods`() {
        assertTrue {  JavaMethod(src2).valid }
    }

    @Test
    fun `no inner methods but anonymous object`() {
        assertTrue {  JavaMethod(src3).valid }
    }

    @Test
    fun `no inner methods because abstract method`() {
        assertFalse {  JavaMethod(src4).valid }
    }

    @Test
    fun `body method with only throw exception should be invalid`() {
        assertFalse {  JavaMethod(src5).valid }
    }

    @Test
    fun `empty method`() {
        assertFalse {  JavaMethod(src6).valid }
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

const val src5 = """
    @Override
public void modifyAVUMetadata(final String userName, final AvuData avuData) throws DataNotFoundException, JargonException {
    throw new UnsupportedOperationException("--##string##--");
}
"""
const val src6 = """
    @FXML
    void satd() {
    }
"""