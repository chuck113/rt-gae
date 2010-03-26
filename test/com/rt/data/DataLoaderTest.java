package com.rt.data;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.rt.web.RhymeUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class DataLoaderTest {

    private final LocalServiceTestHelper helper =
        new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

    private DataLoader loader;
    private RhymeDao dao;

    @Before
    public void setUp() {
        helper.setUp();
        loader = new DataLoader();
        dao = new RhymeDao();
        loader.load(dao);
    }

    @After
    public void tearDown() {
        helper.tearDown();
    }

    @Test
    public void testLoad(){
        Rhyme rhyme = dao.lookUpWord("bump");
        Assert.assertNotNull(rhyme);
        RhymeData data = rhyme.getRhymes().get(0);
        List<String> st = data.getRhymes();
        Assert.assertEquals("Disco bag schlepping and you're doing the bump", st.get(0));
        Assert.assertEquals("Shake your rump", st.get(1));

        List<RhymeUtil.RhymeData> dataList = RhymeUtil.findRhymes("bump");
        RhymeUtil.RhymeData data1 = dataList.get(0);
        System.out.println("DataLoaderTest.testLoad data: "+data1.getTitle());

        Rhyme rhyme2 = dao.lookUpWord("fight");
        Assert.assertEquals(2,rhyme2.getRhymes().size());


    }
}
