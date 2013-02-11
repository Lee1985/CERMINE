package pl.edu.icm.cermine.service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import static org.junit.Assert.*;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.icm.cermine.PdfNLMContentExtractor;
import pl.edu.icm.cermine.exception.AnalysisException;

/**
 *
 * @author Aleksander Nowinski <a.nowinski@icm.edu.pl>
 */
public class CermineExtractorServiceImplTest {

    Logger log = LoggerFactory.getLogger(CermineExtractorServiceImplTest.class);

    public CermineExtractorServiceImplTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of extractNLM method, of class CermineExtractorServiceImpl.
     */
    @Test
    public void testExtractNLM() throws Exception {
        System.out.println("extractNLM");
        InputStream is = this.getClass().getResourceAsStream("/pdf/test1.pdf");
        log.debug("Input stream is: {}", is);
        CermineExtractorServiceImpl instance = new CermineExtractorServiceImpl();
        instance.init();
        ExtractionResult result = instance.extractNLM(is);
        assertNotNull(result);
        assertTrue(result.isSucceeded());

    }

    /**
     * Test of extractNLM method, of class CermineExtractorServiceImpl.
     */
    @Test
    public void testQueue() throws Exception {
        System.out.println("Queue overflow");
        final CermineExtractorServiceImpl instance = new CermineExtractorServiceImpl();
        instance.setThreadPoolSize(1);
        instance.setMaxQueueForBatch(1);
        instance.init();
        final Map<Integer, Boolean> succ = new HashMap<Integer, Boolean>();

        //run immediately two, then 
        List<Thread> threads = new ArrayList<Thread>();
        for (int i = 0; i < 4; i++) {
            final int k = i;
            threads.add(new Thread(new Runnable() {
                @Override
                public void run() {
                    int idx = k;
                    try {
                        succ.put(idx, true);
                        instance.extractNLM(this.getClass().getResourceAsStream("/pdf/test1.pdf"));
                        log.debug("Extraction succeeeded...");
                    } catch (ServiceException ex) {
                        succ.put(idx, false);
                        log.debug("Service exception");
                    } catch (AnalysisException ex) {
                        java.util.logging.Logger.getLogger(CermineExtractorServiceImplTest.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }));

        }
        for (Thread t : threads) {
            t.start();
            Thread.sleep(500);
        }
        for (Thread t : threads) {
            if (t.isAlive()) {
                t.join();
            }
        }
        
        for (int i = 0; i < 2; i++) {
            assertTrue(succ.get(i));
        }
        assertFalse(succ.get(2));
        assertFalse(succ.get(3));

    }
    boolean sleeping = true;

    /**
     * Test of obtainExtractor method, of class CermineExtractorServiceImpl.
     */
    @Test
    public void testObtainExtractor() throws Exception {
        System.out.println("obtainExtractor");
        final CermineExtractorServiceImpl instance = new CermineExtractorServiceImpl();
        instance.setThreadPoolSize(3);
        instance.init();
        List<PdfNLMContentExtractor> list = new ArrayList<PdfNLMContentExtractor>();
        for (int i = 0; i < 3; i++) {
            list.add(instance.obtainExtractor());
        }
        sleeping = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                PdfNLMContentExtractor res = instance.obtainExtractor();
                sleeping = false;
            }
        }).start();
        assertTrue(sleeping);
        Thread.sleep(100);
        assertTrue(sleeping);
        instance.returnExtractor(list.remove(0));
        Thread.sleep(100);
        assertFalse(sleeping);
    }
}
