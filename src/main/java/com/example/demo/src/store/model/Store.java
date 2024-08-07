package com.example.demo.src.store.model;

import com.example.demo.src.menu.Menu;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Store {
    private int storeId;

    private String storeName;

    private String storeAddress;
    private String category;

    private int likeCount;

    private double avgRating;

    private LocalDateTime updateAt;

    List<Menu> menuList;

    // StoreRequest를 위해 추가된 생성자
    public Store(String storeName, String storeAddress, String category) {
        this.storeName = storeName;
        this.storeAddress = storeAddress;
        this.category = category;
    }
}