package satd.step2

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue


class JavaMethodTest {

    //the string src1, src2, ... are below in this file

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

    @Test
    fun `default method`() {
        assertFalse {  JavaMethod(src7).valid }
    }

    @Test
    fun `enum keyword used as variable name`() {
        assertFalse {  JavaMethod(src8).valid }
    }

    @Test
    fun `unexpected ???`() {
        assertFalse {  JavaMethod(src9).valid }
    }

    @Test
    fun `case with multiple values should not be valid to be compatible with cod2vec JavaExtractor`() {
        assertFalse {  JavaMethod(src10).valid }
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

const val src7 = """
   default void fixed(Select.Selection selection, String alias) {
        final List<Object> parameters = parameters();
        if (functionName().equals("--##string##--") && parameters.size() == 1) {
            selection.cast(QueryBuilder.raw(parameters.get(0).toString()), targetCQLTypeName()).as(alias);
        } else {
            selection.fcall(functionName(), parameters.toArray()).as(alias);
        }
    }"""

const val src8 = """
    protected synchronized void satd() {
        super._decreaseActiveCount();
        if (!_processlisteners.isEmpty()) {
            ProcessThread pro = (ProcessThread) Thread.currentThread();
            Actor actor = pro.getActor();
            PNProcessEvent event = new PNProcessEvent(actor, PNProcessEvent.PROCESS_FINISHED, PNProcessEvent.FINISHED_PROPERLY);
            Enumeration enum = _processlisteners.elements();
            while (enum.hasMoreElements()) {
                PNProcessListener lis = (PNProcessListener) enum.nextElement();
                lis.processStateChanged(event);
            }
        }
    }

"""

const val src9 = """
     public void fixed(Record record) {
        long id = store.makeNewRecordId();
        store.registerRecord(id, record);
        for (Property p : config.getIdentityProperties()) ???;
        for (Property p : config.getLookupProperties()) {
            String propname = p.getName();
            for (String value : record.getValues(propname)) {
                String[] tokens = StringUtils.split(value);
                for (int ix = 0; ix < tokens.length; ix++) store.registerToken(id, propname, tokens[ix]);
            }
        }
    }
"""

const val src10 ="""
    @Override
    public void fixed() {
        switch(_skillType) {
            case TRANSFORM, FISHING:
                client.sendPacket(new AcquireSkillInfo(_skillType, s));
            case CLASS:
                client.sendPacket(new ExAcquireSkillInfo(activeChar, s)); 
        }
    }
"""