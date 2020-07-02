package fudaojun.com.rxjava.newdemo.module;

public class Hourse {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    private int price;

    @Override
    public String toString() {
        return "Hourse{" +
                "name='" + name + '\'' +
                ", price=" + price +
                '}';
    }
}
