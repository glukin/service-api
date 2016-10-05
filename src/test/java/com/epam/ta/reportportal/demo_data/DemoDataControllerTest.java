/*
 * Copyright 2016 EPAM Systems
 *
 *
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/service-api
 *
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.epam.ta.reportportal.demo_data;

import static com.epam.ta.reportportal.auth.AuthConstants.ADMINISTRATOR;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;

import com.epam.ta.reportportal.auth.AuthConstants;
import com.epam.ta.reportportal.database.dao.DashboardRepository;
import com.epam.ta.reportportal.database.dao.LaunchRepository;
import com.epam.ta.reportportal.database.dao.UserFilterRepository;
import com.epam.ta.reportportal.database.entity.Dashboard;
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.filter.UserFilter;
import com.epam.ta.reportportal.ws.BaseMvcTest;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DemoDataControllerTest extends BaseMvcTest {

	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private LaunchRepository launchRepository;
	@Autowired
	private DashboardRepository dashboardRepository;
	@Autowired
	private UserFilterRepository userFilterRepository;

	@Test
	public void generateDemoData() throws Exception {
		DemoDataRq rq = new DemoDataRq();
		rq.setCreateDashboard(true);
		rq.setLaunchesQuantity(1);
		mvcMock.perform(post("/demo/project1").content(objectMapper.writeValueAsBytes(rq)).contentType(APPLICATION_JSON)
				.principal(authentication())).andExpect(status().is(200));
		List<Launch> byName = launchRepository.findByName(rq.getLaunchName());
		Assert.assertFalse(byName.isEmpty());
		Assert.assertEquals(rq.getLaunchesQuantity(), byName.size());
		Dashboard dashboard = dashboardRepository.findOneByUserProject(AuthConstants.TEST_USER, "project1", rq.getDashboardName());
		Assert.assertNotNull(dashboard);
		Assert.assertEquals(9, dashboardRepository.findOne(dashboard.getId()).getWidgets().size());
		final UserFilter filter = userFilterRepository.findOneByName(AuthConstants.TEST_USER, rq.getFilterName(), "project1");
		Assert.assertNotNull(filter);

	}

	@Override
	protected Authentication authentication() {
		return ADMINISTRATOR;
	}
}