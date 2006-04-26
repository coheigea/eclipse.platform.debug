/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.debug.internal.ui.actions.context;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.ISuspendResume;
import org.eclipse.debug.internal.ui.actions.provisional.IAsynchronousSuspendResumeAdapter;
import org.eclipse.debug.internal.ui.actions.provisional.IBooleanRequestMonitor;
import org.eclipse.debug.internal.ui.viewers.provisional.IAsynchronousRequestMonitor;
import org.eclipse.debug.ui.IDebugUIConstants;

public class SuspendResumeAdapter extends StandardActionAdapter implements IAsynchronousSuspendResumeAdapter {

	public void canResume(final Object element, final IBooleanRequestMonitor monitor) {
		Job job = new Job("canResume") { //$NON-NLS-1$
			protected IStatus run(IProgressMonitor pm) {
				if (!pm.isCanceled()) {
					ISuspendResume suspendResume = getTarget(element);
					if (suspendResume != null)
						monitor.setResult(suspendResume.canResume());
					else
						monitor.setResult(false);
					monitor.done();
				}
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.setRule(createUpdateSchedulingRule());
		job.schedule();
	}

	public void canSuspend(final Object element, final IBooleanRequestMonitor monitor) {
		Job job = new Job("canSuspend") { //$NON-NLS-1$
			protected IStatus run(IProgressMonitor pm) {
				if (!pm.isCanceled()) {
					ISuspendResume suspendResume = getTarget(element);
					if (suspendResume != null)
						monitor.setResult(suspendResume.canSuspend());
					else
						monitor.setResult(false);
					monitor.done();
				}
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.setRule(createUpdateSchedulingRule());
		job.schedule();
	}

	public void isSuspended(final Object element, final IBooleanRequestMonitor monitor) {
		Job job = new Job("isSuspended") { //$NON-NLS-1$
			protected IStatus run(IProgressMonitor pm) {
				if (!pm.isCanceled()) {
					ISuspendResume suspendResume = getTarget(element);
					if (suspendResume != null)
						monitor.setResult(suspendResume.isSuspended());
					else
						monitor.setResult(false);
					monitor.done();
				}
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.setRule(createUpdateSchedulingRule());
		job.schedule();
	}

	public void resume(final Object element, final IAsynchronousRequestMonitor monitor) {
		Job job = new Job("resume") { //$NON-NLS-1$
			protected IStatus run(IProgressMonitor pm) {
				if (!pm.isCanceled()) {
					ISuspendResume suspendResume = getTarget(element);
					if (suspendResume != null) {
						try {
							suspendResume.resume();
						} catch (DebugException e) {
							monitor.setStatus(e.getStatus());
						}
					} else {
						monitor.setStatus(new Status(IStatus.ERROR, IDebugUIConstants.PLUGIN_ID, IDebugUIConstants.INTERNAL_ERROR, "element must be an instance of or adapt to ISuspendResume", //$NON-NLS-1$
								null));
					}
					monitor.done();
				}
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.schedule();
	}

	public void suspend(final Object element, final IAsynchronousRequestMonitor monitor) {
		Job job = new Job("suspend") { //$NON-NLS-1$
			protected IStatus run(IProgressMonitor pm) {
				if (!pm.isCanceled()) {
					ISuspendResume suspendResume = getTarget(element);
					if (suspendResume != null) {
						try {
							suspendResume.suspend();
						} catch (DebugException e) {
							monitor.setStatus(e.getStatus());
						}
					} else {
						monitor.setStatus(new Status(IStatus.ERROR, IDebugUIConstants.PLUGIN_ID, IDebugUIConstants.INTERNAL_ERROR, "element must be an instance of or adapt to ISuspendResume", //$NON-NLS-1$
								null));
					}
					monitor.done();
				}
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.schedule();
	}

	private ISuspendResume getTarget(Object element) {
		if (element instanceof ISuspendResume) {
			return (ISuspendResume) element;
		} else if (element instanceof IAdaptable) {
			return (ISuspendResume) ((IAdaptable) element).getAdapter(ISuspendResume.class);
		}
		return null;
	}

}
