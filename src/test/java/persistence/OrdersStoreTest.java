package persistence;

import model.Order;
import org.apache.commons.dbcp2.BasicDataSource;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class OrdersStoreTest {

    private BasicDataSource pool = new BasicDataSource();

    @Before
    public void setUp() throws SQLException {
        pool.setDriverClassName("org.hsqldb.jdbcDriver");
        pool.setUrl("jdbc:hsqldb:mem:tests;sql.syntax_pgs=true");
        pool.setUsername("sa");
        pool.setPassword("");
        pool.setMaxTotal(2);
        StringBuilder builder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream("./db/update_001.sql")))
        ) {
            br.lines().forEach(line -> builder.append(line).append(System.lineSeparator()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        pool.getConnection().prepareStatement(builder.toString()).executeUpdate();
    }

    @Test
    public void whenSaveOrderAndFindAllOneRowWithDescription() {
        OrdersStore store = new OrdersStore(pool);

        store.save(Order.of("name1", "description1"));

        List<Order> all = (List<Order>) store.findAll();
        System.out.println(all.size());
        assertThat(all.size(), is(1));
        assertThat(all.get(0).getDescription(), is("description1"));
        assertThat(all.get(0).getId(), is(1));
    }


    @Test
    public void whenUpdate() {
        OrdersStore store = new OrdersStore(pool);
        Order first = store.save(Order.of("name1", "description1"));
        Order model = store.findById(1);

        System.out.println(" Original Name : " + model.getName());
        System.out.println(" Original id : " + model.getId());
        Order test = new Order(first.getId(), "Test", first.getDescription(), first.getCreated());
        store.update(test);
        List<Order> all = (List<Order>) store.findAll();
        assertThat(all.get(0).getName(), is("Test"));
        assertThat(all.get(0).getDescription(), is("description1"));
    }

    @Test
    public void whenFindByName() {
        OrdersStore store = new OrdersStore(pool);
        String testName = "Test";
        Order first = store.save(Order.of(testName, "description1"));
        Order testO = store.findById(1);
        Order model = store.findByName(testName);

        assertThat(model, is(testO));
    }

}