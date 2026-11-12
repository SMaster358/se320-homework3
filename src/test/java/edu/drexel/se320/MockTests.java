package edu.drexel.se320;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import static org.mockito.Mockito.*;
import java.util.List;
import java.io.IOException;

public class MockTests {
    /**
     * I had a couple reasons why I chose to use the collaborator factory method when approaching this homework.
     * Most simply, the fact that the instructions to the homework explicitly mention adapting the collaborator factory for use in this assignment. Given that, in my eyes, this is an implicit suggestion or at the very least confirmation that it will work for this assignment, I figured it made the most sense not to overcomplicate the process and just use the method.
     * On a more technical level, I chose the collaborator factory method due to the fact that it streamlined the process of writing tests. Because I could just overwrite the generateConnection method in the tests, it made the amount of refactoring in the client class minimal.
     * When comparing this method to others available, the collaborator factory required that I change the minimal amount of the structure of the client class as was possible, allowing for as easy of a refactor as I saw possible.
     * Even though this implementation led to some redundancies in the test code, given the fact that I refactored and decided on an implementation before writing the tests, it seemed to me that changing the core structure of the class as minimally as possible would make writing the tests as quick and easy as possible.
     * Ultimately, my decision to use the collaborator factory method boiled down to the adaptability it allowed me to have, even though I didn't end up needing it for these tests, as well as the ease of implementation allowing me to most quickly grasp the functionality of the client class.
     */

    public MockTests() {}

    /**
     * Demonstrate a working mock from the Mockito documentation.
     * https://static.javadoc.io/org.mockito/mockito-core/latest/org/mockito/Mockito.html#1
     */
    @Test
    public void testMockDemo() {
        List<String> mockedList = (List<String>)mock(List.class);

        mockedList.add("one");
        mockedList.clear();

        verify(mockedList).add("one");
        verify(mockedList).clear();
    }

    @Test
    public void testServerConnectionFailureGivesNull() throws IOException {
        ServerConnection sc = mock(ServerConnection.class);
        Client c = new Client() {
            @Override
            protected ServerConnection generateConnection() {
                return sc;
            }
        };
        when(sc.connectTo(anyString())).thenReturn(false);

        // If you change the code to pass the mock above to the client (based on your choice of
        // refactoring), this test should pass.  Until then, it will fail.
        assertNull(c.requestFile("DUMMY", "DUMMY"));
    }

    /**
     * The first test listed in README.md, all tests will follow the order that they appear in the file.
     * Waits for a failed connection and checks that no interactions occur subsequently
     */
    @Test
    public void test1() throws IOException {
        ServerConnection sc = mock(ServerConnection.class);
        Client c = new Client() {
            @Override
            protected ServerConnection generateConnection() {
                return sc;
            }
        };
        when(sc.connectTo(anyString())).thenReturn(false);

        c.requestFile("DUMMY","DUMMY");

        verify(sc).connectTo(anyString());
        verifyNoMoreInteractions(sc);
    }

    /**
     *Confirms that the connection is successful, then confirms that specifically the request for the file content fails
     * Verification checks that the connection succeeded, the file content call failed, and that the ONLY other call is to close the connection
     */
    @Test
    public void test2() throws IOException{
        ServerConnection sc = mock(ServerConnection.class);

        Client c = new Client() {
            @Override
            protected ServerConnection generateConnection() {
                return sc;
            }
        };

        when(sc.connectTo(anyString())).thenReturn(true);
        when(sc.requestFileContents(anyString())).thenReturn(false);

        c.requestFile("DUMMY", "DUMMY");

        verify(sc).connectTo(anyString());
        verify(sc).requestFileContents(anyString());
        verify(sc).closeConnection();
        verifyNoMoreInteractions(sc);
    }

    /**
     *Confirms that the connection succeeds, then that accessing the file succeeds, then that the file has content, and finally that the client gets some value when it calls to read
     * after all of this we confirm with the verify statement that the client called the read function, so we don't have to check the output
     */
    @Test
    public void test3() throws IOException {
        ServerConnection sc = mock(ServerConnection.class);

        Client c = new Client() {
            @Override
            protected ServerConnection generateConnection() {
                return sc;
            }
        };

        when(sc.connectTo(anyString())).thenReturn(true);
        when(sc.requestFileContents(anyString())).thenReturn(true);
        when (sc.moreBytes()).thenReturn(true, false);
        when (sc.read()).thenReturn("DUMMY");

        c.requestFile("DUMMY", "DUMMY");
        verify(sc, atLeastOnce()).read();
    }

    /**
     *Confirms that the connection is successful, then that the file is accessible, then that the file has no content to read
     *then confirms that the output is an empty string
     */

    @Test
    public void test4() throws IOException {
        ServerConnection sc = mock(ServerConnection.class);

        Client c = new Client() {
            @Override
            protected ServerConnection generateConnection() {
                return sc;
            }
        };

        when(sc.connectTo(anyString())).thenReturn(true);
        when(sc.requestFileContents(anyString())).thenReturn(true);
        when (sc.moreBytes()).thenReturn(false);

        var actual = c.requestFile("DUMMY", "DUMMY");
        assertEquals("", actual);
    }

    /**
     * confirms that the connection succeeds, then that the file is accessible, then that the file has content, and finally that trying to read some portion file throws an exception
     * asserts that upon failure the output will be null
     */
    @Test
    public void test5() throws IOException {
        ServerConnection sc = mock(ServerConnection.class);

        Client c = new Client() {
            @Override
            protected ServerConnection generateConnection() {
                return sc;
            }
        };

        when(sc.connectTo(anyString())).thenReturn(true);
        when(sc.requestFileContents(anyString())).thenReturn(true);
        when (sc.moreBytes()).thenReturn(true);
        when (sc.read()).thenReturn("DUMMY").thenThrow(new IOException());

        var actual = c.requestFile("DUMMY", "DUMMY");
        assertNull(actual);
    }

    /**
     *confirms that the connection is successful, then that the client fails to request the content of the file
     * verifies that the connection then closes and that the output is null
     */
    @Test
    public void test6() throws IOException{
        ServerConnection sc = mock(ServerConnection.class);

        Client c = new Client() {
            @Override
            protected ServerConnection generateConnection() {
                return sc;
            }
        };

        when(sc.connectTo(anyString())).thenReturn(true);
        when(sc.requestFileContents(anyString())).thenThrow(new IOException());

        var output = c.requestFile("DUMMY", "DUMMY");
        assertNull(output);
        verify(sc).closeConnection();
    }

    /**
     * confirms that the connection is successful, then that the file content request is successful, then the file has contents and is not empty, and then that reading the contents succeeds
     * creates an inorder object to verify that the execution of the methods were in the correct order
     */
    @Test
    public void test7() throws IOException {
        ServerConnection sc = mock(ServerConnection.class);

        Client c = new Client() {
            @Override
            protected ServerConnection generateConnection() {
                return sc;
            }
        };

        when(sc.connectTo(anyString())).thenReturn(true);
        when(sc.requestFileContents(anyString())).thenReturn(true);
        when(sc.moreBytes()).thenReturn(true, false);
        when(sc.read()).thenReturn("DUMMY");

        c.requestFile("DUMMY", "DUMMY");
        InOrder inOrder = inOrder(sc);
        inOrder.verify(sc).moreBytes();
        inOrder.verify(sc).read();
    }

    /**
     * confirms that the connection succeeds, then that the file is accessible, then that the content of the file spans across 4 moreByte 'checks' in sequence, then confirms that the content of the file is read properly
     * then asserts that the output of the call is equal to the combination of all 4 read 'calls' in order
     */
    @Test
    public void test8() throws IOException {
        ServerConnection sc = mock(ServerConnection.class);

        Client c = new Client() {
            @Override
            protected ServerConnection generateConnection() {
                return sc;
            }
        };

        when(sc.connectTo(anyString())).thenReturn(true);
        when(sc.requestFileContents(anyString())).thenReturn(true);
        when(sc.moreBytes()).thenReturn(true, true, true, true, false);
        when(sc.read()).thenReturn("1", "2", "3", "4");

        var actual = c.requestFile("DUMMY", "DUMMY");
        assertEquals("1234", actual);
    }

    /**
     *confirms that the connection succeeds, then that the file content is accessible, then that the content of the file is not empty, then that reading the content at some point returns null
     * asserts that in the event that the file content returns null, that the output of the call is an empty string
     */
    @Test
    public void test9() throws IOException {
        ServerConnection sc = mock(ServerConnection.class);

        Client c = new Client() {
            @Override
            protected ServerConnection generateConnection() {
                return sc;
            }
        };

        when(sc.connectTo(anyString())).thenReturn(true);
        when(sc.requestFileContents(anyString())).thenReturn(true);
        when(sc.moreBytes()).thenReturn(true, false);
        when(sc.read()).thenReturn(null);

        var actual = c.requestFile("DUMMY", "DUMMY");
        assertEquals("", actual);
    }

    /**
     *confirms that the connection throws an exception
     * asserts that the output of the call is null
     */
    @Test
    public void test10a() throws IOException {
        ServerConnection sc = mock(ServerConnection.class);

        Client c = new Client() {
            @Override
            protected ServerConnection generateConnection() {
                return sc;
            }
        };

        when(sc.connectTo(anyString())).thenThrow(new IOException());

        var actual = c.requestFile("DUMMY", "DUMMY");
        assertNull(actual);
    }

    /**
     * confirms that the connection is successful, then that accessing the file throws an exception
     * asserts that in this scenario the output is null
     */
    @Test
    public void test10b() throws IOException {
        ServerConnection sc = mock(ServerConnection.class);

        Client c = new Client() {
            @Override
            protected ServerConnection generateConnection() {
                return sc;
            }
        };

        when(sc.connectTo(anyString())).thenReturn(true);
        when(sc.requestFileContents(anyString())).thenThrow(new IOException());

        var actual = c.requestFile("DUMMY", "DUMMY");
        assertNull(actual);
    }

    /**
     *confirms that the connection is successful, and the file accessing is successful, then that the contents of the file is not empty, and then that attempting to read the file throws an exception
     * asserts that the output in this situation is null
     */
    @Test
    public void test10c() throws IOException {
        ServerConnection sc = mock(ServerConnection.class);

        Client c = new Client() {
            @Override
            protected ServerConnection generateConnection() {
                return sc;
            }
        };

        when(sc.connectTo(anyString())).thenReturn(true);
        when(sc.requestFileContents(anyString())).thenReturn(true);
        when(sc.moreBytes()).thenReturn(true);
        when(sc.read()).thenThrow(new IOException());

        var actual = c.requestFile("DUMMY", "DUMMY");
        assertNull(actual);
    }

    /**
     *confirms that the connection succeeds, then that the file is able to be accessed, then that getting the contents of the file throws an exception
     * asserts that the output of this sequence is null
     */
    @Test
    public void test10d() throws IOException {
        ServerConnection sc = mock(ServerConnection.class);

        Client c = new Client() {
            @Override
            protected ServerConnection generateConnection() {
                return sc;
            }
        };

        when(sc.connectTo(anyString())).thenReturn(true);
        when(sc.requestFileContents(anyString())).thenReturn(true);
        when(sc.moreBytes()).thenThrow(new IOException());

        var actual = c.requestFile("DUMMY", "DUMMY");
        assertNull(actual);
    }

    /**
     * confirms that the connection succeeds, then that the file is accessed properly, uses doThrow to check that closing the connection throws an exception since it didn't work the same way as the other functions
     * asserts that the output of this sequence is null
     */
    @Test
    public void test10e() throws IOException {
        ServerConnection sc = mock(ServerConnection.class);

        Client c = new Client() {
            @Override
            protected ServerConnection generateConnection() {
                return sc;
            }
        };

        when(sc.connectTo(anyString())).thenReturn(true);
        when(sc.requestFileContents(anyString())).thenReturn(true);
        doThrow(new IOException()).when(sc).closeConnection();

        var actual = c.requestFile("DUMMY", "DUMMY");
        assertNull(actual);
    }
}