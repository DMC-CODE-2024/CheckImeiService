/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.gl.ceir.config.repository.app;

import com.gl.ceir.config.model.app.User;
import com.gl.ceir.config.model.app.UserFeatureIpAccessList;
import org.springframework.data.jpa.repository.JpaRepository;


public interface UserFeatureIpAccessListRepository extends JpaRepository<UserFeatureIpAccessList, Integer> {

    public UserFeatureIpAccessList getByFeatureIpListIdAndUserId(int featureIpListId, int id);
}
