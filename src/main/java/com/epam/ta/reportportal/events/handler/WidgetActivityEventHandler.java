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
package com.epam.ta.reportportal.events.handler;

import com.epam.ta.reportportal.database.dao.ActivityRepository;
import com.epam.ta.reportportal.database.entity.item.Activity;
import com.epam.ta.reportportal.database.entity.widget.Widget;
import com.epam.ta.reportportal.events.WidgetCreatedEvent;
import com.epam.ta.reportportal.events.WidgetDeletedEvent;
import com.epam.ta.reportportal.events.WidgetUpdatedEvent;
import com.epam.ta.reportportal.ws.converter.builders.ActivityBuilder;
import com.epam.ta.reportportal.ws.model.widget.WidgetRQ;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.epam.ta.reportportal.database.entity.item.ActivityEventType.*;
import static com.epam.ta.reportportal.database.entity.item.ActivityObjectType.WIDGET;
import static com.epam.ta.reportportal.events.handler.EventHandlerUtil.*;

/**
 * @author Andrei Varabyeu
 */
@Component
public class WidgetActivityEventHandler {

	private ActivityRepository activityRepository;

	@Autowired
    public WidgetActivityEventHandler(ActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    @EventListener
	public void onWidgetUpdated(WidgetUpdatedEvent event) {
		Widget widget = event.getBefore();
		WidgetRQ widgetRQ = event.getWidgetRQ();
        List<Activity.FieldValues> history = Lists.newArrayList();
		if (widget != null) {
		    processShare(history, widget, widgetRQ.getShare());
            processName(history, widget.getName(), widgetRQ.getName());
            processDescription(history, widget.getDescription(), widgetRQ.getDescription());
            if (!history.isEmpty()) {
                Activity activityLog = new ActivityBuilder()
                        .addProjectRef(widget.getProjectName())
                        .addObjectName(widget.getName())
                        .addObjectType(WIDGET)
                        .addActionType(UPDATE_WIDGET)
                        .addLoggedObjectRef(widget.getId())
                        .addUserRef(event.getUpdatedBy())
                        .get();
                activityLog.setHistory(history);
                activityRepository.save(activityLog);
            }
        }
	}

	@EventListener
    public void onCreateWidget(WidgetCreatedEvent event) {
        WidgetRQ widgetRQ = event.getWidgetRQ();
        Activity activityLog = new ActivityBuilder()
                .addActionType(CREATE_WIDGET)
                .addObjectType(WIDGET)
                .addObjectName(widgetRQ.getName())
                .addProjectRef(event.getProjectRef())
                .addUserRef(event.getCreatedBy())
                .addLoggedObjectRef(event.getWidgetId())
                .addHistory(ImmutableList.<Activity.FieldValues>builder()
                        .add(createHistoryField(NAME, EMPTY_FIELD, widgetRQ.getName()))
                        .build())
                .get();
        activityRepository.save(activityLog);

    }

    @EventListener
    public void onDeleteWidget(WidgetDeletedEvent event) {
        Widget widget = event.getBefore();
        Activity activityLog = new ActivityBuilder()
                .addActionType(DELETE_WIDGET)
                .addObjectType(WIDGET)
                .addObjectName(widget.getName())
                .addProjectRef(widget.getProjectName())
                .addUserRef(event.getRemovedBy())
                .addHistory(ImmutableList.<Activity.FieldValues>builder()
                        .add(createHistoryField(NAME, widget.getName(), EMPTY_FIELD)).build())
                .get();
        activityRepository.save(activityLog);
    }

}

