/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.ide.upgrade.tasks.core.internal.code;

import com.liferay.ide.upgrade.plan.core.BaseUpgradeTaskStepAction;
import com.liferay.ide.upgrade.plan.core.UpgradePlanElementStatus;
import com.liferay.ide.upgrade.plan.core.UpgradeTaskStepAction;
import com.liferay.ide.upgrade.tasks.core.code.ConfigureLiferayWorkspaceSettingsStepKeys;
import com.liferay.ide.upgrade.tasks.core.code.ConfigureTargetPlatformActionKeys;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * @author Terry Jia
 * @author Gregory Amerson
 */
@Component(
	property = {
		"description=" + ConfigureTargetPlatformActionKeys.DESCRIPTION, "id=" + ConfigureTargetPlatformActionKeys.ID,
		"requirement=optional", "order=3", "stepId=" + ConfigureLiferayWorkspaceSettingsStepKeys.ID,
		"title=" + ConfigureTargetPlatformActionKeys.TITLE
	},
	scope = ServiceScope.PROTOTYPE, service = UpgradeTaskStepAction.class
)
public class ConfigureTargetPlatformAction extends BaseUpgradeTaskStepAction {

	@Override
	public IStatus perform(IProgressMonitor progressMonitor) {
		setStatus(UpgradePlanElementStatus.COMPLETED);

		return Status.OK_STATUS;
	}

}