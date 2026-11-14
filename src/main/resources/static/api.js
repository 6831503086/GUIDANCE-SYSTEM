/* Routes extracted from the supplied Java controllers. No UI or fixture changes. */
window.MfuApi = (() => {
const routes = {
  "AdminController.getAll": {
    "method": "GET",
    "path": "/api/admins"
  },
  "AdminController.getById": {
    "method": "GET",
    "path": "/api/admins/{id}"
  },
  "AdminController.create": {
    "method": "POST",
    "path": "/api/admins"
  },
  "AdminController.update": {
    "method": "PUT",
    "path": "/api/admins/{id}"
  },
  "AdminController.delete": {
    "method": "DELETE",
    "path": "/api/admins/{id}"
  },
  "AdminDashboardController.getDashboard": {
    "method": "GET",
    "path": "/api/admin/dashboard"
  },
  "AdminSchoolOverviewController.getSchoolOverview": {
    "method": "GET",
    "path": "/api/admin/schools/overview"
  },
  "ApplicantController.getAll": {
    "method": "GET",
    "path": "/api/applicants"
  },
  "ApplicantController.getById": {
    "method": "GET",
    "path": "/api/applicants/{id}"
  },
  "ApplicantController.create": {
    "method": "POST",
    "path": "/api/applicants"
  },
  "ApplicantController.update": {
    "method": "PUT",
    "path": "/api/applicants/{id}"
  },
  "ApplicantController.delete": {
    "method": "DELETE",
    "path": "/api/applicants/{id}"
  },
  "ApplicantController.searchStudents": {
    "method": "GET",
    "path": "/api/applicants/search"
  },
  "ApplicantOverviewReportController.getReport": {
    "method": "GET",
    "path": "/api/reports/applicant-overview"
  },
  "ApplicantOverviewReportController.exportPdf": {
    "method": "GET",
    "path": "/api/reports/applicant-overview/export/pdf"
  },
  "ApplicantSelectionController.getAll": {
    "method": "GET",
    "path": "/api/applicant-selections"
  },
  "ApplicantSelectionController.getById": {
    "method": "GET",
    "path": "/api/applicant-selections/{id}"
  },
  "ApplicantSelectionController.create": {
    "method": "POST",
    "path": "/api/applicant-selections"
  },
  "ApplicantSelectionController.update": {
    "method": "PUT",
    "path": "/api/applicant-selections/{id}"
  },
  "ApplicantSelectionController.delete": {
    "method": "DELETE",
    "path": "/api/applicant-selections/{id}"
  },
  "ApplicantStatusController.getAll": {
    "method": "GET",
    "path": "/api/applicant-statuses"
  },
  "ApplicantStatusController.getById": {
    "method": "GET",
    "path": "/api/applicant-statuses/{code}"
  },
  "ApplicantStatusController.create": {
    "method": "POST",
    "path": "/api/applicant-statuses"
  },
  "ApplicantStatusController.update": {
    "method": "PUT",
    "path": "/api/applicant-statuses/{code}"
  },
  "ApplicantStatusController.delete": {
    "method": "DELETE",
    "path": "/api/applicant-statuses/{code}"
  },
  "CalendarController.getCalendarEvents": {
    "method": "GET",
    "path": "/api/calendar/events"
  },
  "CalendarController.getUpcomingEvents": {
    "method": "GET",
    "path": "/api/calendar/events/upcoming"
  },
  "CalendarController.getMonthlyEvents": {
    "method": "GET",
    "path": "/api/calendar/events/monthly"
  },
  "CalendarController.getEventById": {
    "method": "GET",
    "path": "/api/calendar/events/{eventId}"
  },
  "CalendarController.downloadCalendarPdf": {
    "method": "GET",
    "path": "/api/calendar/events/pdf"
  },
  "DistrictController.getAll": {
    "method": "GET",
    "path": "/api/districts"
  },
  "DistrictController.getById": {
    "method": "GET",
    "path": "/api/districts/{id}"
  },
  "DistrictController.create": {
    "method": "POST",
    "path": "/api/districts"
  },
  "DistrictController.update": {
    "method": "PUT",
    "path": "/api/districts/{id}"
  },
  "DistrictController.delete": {
    "method": "DELETE",
    "path": "/api/districts/{id}"
  },
  "EmailVerificationController.verify": {
    "method": "GET",
    "path": "/api/email-verification/verify"
  },
  "EntryGroupController.getAll": {
    "method": "GET",
    "path": "/api/entry-groups"
  },
  "EntryGroupController.getById": {
    "method": "GET",
    "path": "/api/entry-groups/{id}"
  },
  "EntryGroupController.create": {
    "method": "POST",
    "path": "/api/entry-groups"
  },
  "EntryGroupController.update": {
    "method": "PUT",
    "path": "/api/entry-groups/{id}"
  },
  "EntryGroupController.delete": {
    "method": "DELETE",
    "path": "/api/entry-groups/{id}"
  },
  "GuidanceTeacherController.getAll": {
    "method": "GET",
    "path": "/api/guidance-teachers"
  },
  "GuidanceTeacherController.getById": {
    "method": "GET",
    "path": "/api/guidance-teachers/{id}"
  },
  "GuidanceTeacherController.create": {
    "method": "POST",
    "path": "/api/guidance-teachers"
  },
  "GuidanceTeacherController.getMyProfile": {
    "method": "GET",
    "path": "/api/guidance-teachers/me"
  },
  "GuidanceTeacherController.renewMyAcademicYear": {
    "method": "PUT",
    "path": "/api/guidance-teachers/me/renew"
  },
  "GuidanceTeacherController.updateMyProfile": {
    "method": "PUT",
    "path": "/api/guidance-teachers/me"
  },
  "GuidanceTeacherController.uploadMyProfilePicture": {
    "method": "POST",
    "path": "/api/guidance-teachers/me/profile-picture"
  },
  "GuidanceTeacherController.requestEmailChange": {
    "method": "PUT",
    "path": "/api/guidance-teachers/me/email"
  },
  "GuidanceTeacherController.deleteMyAccount": {
    "method": "DELETE",
    "path": "/api/guidance-teachers/me"
  },
  "GuidanceTeacherController.update": {
    "method": "PUT",
    "path": "/api/guidance-teachers/{id}"
  },
  "GuidanceTeacherController.delete": {
    "method": "DELETE",
    "path": "/api/guidance-teachers/{id}"
  },
  "LoginController.login": {
    "method": "POST",
    "path": "/api/auth/login"
  },
  "LoginController.forgotPassword": {
    "method": "POST",
    "path": "/api/auth/forgot-password"
  },
  "LoginController.resetPassword": {
    "method": "POST",
    "path": "/api/auth/reset-password"
  },
  "PostController.getAll": {
    "method": "GET",
    "path": "/api/posts"
  },
  "PostController.getAllForAdmin": {
    "method": "GET",
    "path": "/api/posts/admin/all"
  },
  "PostController.getById": {
    "method": "GET",
    "path": "/api/posts/{id}"
  },
  "PostController.create": {
    "method": "POST",
    "path": "/api/posts"
  },
  "PostController.update": {
    "method": "PUT",
    "path": "/api/posts/{id}"
  },
  "PostController.publish": {
    "method": "POST",
    "path": "/api/posts/{id}/publish"
  },
  "PostController.unpublish": {
    "method": "PATCH",
    "path": "/api/posts/{id}/unpublish"
  },
  "PostController.hide": {
    "method": "PATCH",
    "path": "/api/posts/{id}/hide"
  },
  "PostController.show": {
    "method": "PATCH",
    "path": "/api/posts/{id}/show"
  },
  "PostController.delete": {
    "method": "DELETE",
    "path": "/api/posts/{id}"
  },
  "PostPinController.getByTeacher": {
    "method": "GET",
    "path": "/api/post-pins/teacher/{teacherId}"
  },
  "PostPinController.pinPost": {
    "method": "POST",
    "path": "/api/post-pins"
  },
  "PostPinController.unpinPost": {
    "method": "DELETE",
    "path": "/api/post-pins/{postId}/{teacherId}"
  },
  "PostRegistrationController.getAll": {
    "method": "GET",
    "path": "/api/post-registrations"
  },
  "PostRegistrationController.getByTeacher": {
    "method": "GET",
    "path": "/api/post-registrations/teacher/{teacherId}"
  },
  "PostRegistrationController.getByPost": {
    "method": "GET",
    "path": "/api/post-registrations/post/{postId}"
  },
  "PostRegistrationController.register": {
    "method": "POST",
    "path": "/api/post-registrations"
  },
  "PostRegistrationController.cancel": {
    "method": "DELETE",
    "path": "/api/post-registrations/{registrationId}"
  },
  "PrefixController.getAll": {
    "method": "GET",
    "path": "/api/prefixes"
  },
  "PrefixController.getById": {
    "method": "GET",
    "path": "/api/prefixes/{id}"
  },
  "PrefixController.create": {
    "method": "POST",
    "path": "/api/prefixes"
  },
  "PrefixController.update": {
    "method": "PUT",
    "path": "/api/prefixes/{id}"
  },
  "PrefixController.delete": {
    "method": "DELETE",
    "path": "/api/prefixes/{id}"
  },
  "ProgramController.getAll": {
    "method": "GET",
    "path": "/api/programs"
  },
  "ProgramController.getById": {
    "method": "GET",
    "path": "/api/programs/{id}"
  },
  "ProgramController.create": {
    "method": "POST",
    "path": "/api/programs"
  },
  "ProgramController.update": {
    "method": "PUT",
    "path": "/api/programs/{id}"
  },
  "ProgramController.delete": {
    "method": "DELETE",
    "path": "/api/programs/{id}"
  },
  "ProgramInterestController.getAll": {
    "method": "GET",
    "path": "/api/program-interests"
  },
  "ProgramInterestController.getById": {
    "method": "GET",
    "path": "/api/program-interests/{id}"
  },
  "ProgramInterestController.create": {
    "method": "POST",
    "path": "/api/program-interests"
  },
  "ProgramInterestController.update": {
    "method": "PUT",
    "path": "/api/program-interests/{id}"
  },
  "ProgramInterestController.delete": {
    "method": "DELETE",
    "path": "/api/program-interests/{id}"
  },
  "ProgramInterestController.analyze": {
    "method": "GET",
    "path": "/api/program-interests/analysis"
  },
  "ProgramInterestController.exportAnalysisCsv": {
    "method": "GET",
    "path": "/api/program-interests/analysis/export/csv"
  },
  "ProgramInterestController.exportAnalysisPdf": {
    "method": "GET",
    "path": "/api/program-interests/analysis/export/pdf"
  },
  "ProvinceController.getAll": {
    "method": "GET",
    "path": "/api/provinces"
  },
  "ProvinceController.getById": {
    "method": "GET",
    "path": "/api/provinces/{id}"
  },
  "ProvinceController.create": {
    "method": "POST",
    "path": "/api/provinces"
  },
  "ProvinceController.update": {
    "method": "PUT",
    "path": "/api/provinces/{id}"
  },
  "ProvinceController.delete": {
    "method": "DELETE",
    "path": "/api/provinces/{id}"
  },
  "QuotaController.getAll": {
    "method": "GET",
    "path": "/api/quotas"
  },
  "QuotaController.getById": {
    "method": "GET",
    "path": "/api/quotas/{id}"
  },
  "QuotaController.create": {
    "method": "POST",
    "path": "/api/quotas"
  },
  "QuotaController.update": {
    "method": "PUT",
    "path": "/api/quotas/{id}"
  },
  "QuotaController.delete": {
    "method": "DELETE",
    "path": "/api/quotas/{id}"
  },
  "ResultDatasetController.getDatasets": {
    "method": "GET",
    "path": "/api/admin/result-datasets"
  },
  "ResultDatasetController.getSummary": {
    "method": "GET",
    "path": "/api/admin/result-datasets/summary"
  },
  "ResultDatasetController.getDatasetDetails": {
    "method": "GET",
    "path": "/api/admin/result-datasets/{id}"
  },
  "SchoolController.getAllSchools": {
    "method": "GET",
    "path": "/api/schools"
  },
  "SchoolController.getSchoolById": {
    "method": "GET",
    "path": "/api/schools/{id}"
  },
  "SchoolController.createSchool": {
    "method": "POST",
    "path": "/api/schools"
  },
  "SchoolController.updateSchool": {
    "method": "PUT",
    "path": "/api/schools/{id}"
  },
  "SchoolController.deleteSchool": {
    "method": "DELETE",
    "path": "/api/schools/{id}"
  },
  "SchoolController.getMySchool": {
    "method": "GET",
    "path": "/api/schools/me"
  },
  "SchoolController.updateMySchool": {
    "method": "PUT",
    "path": "/api/schools/me"
  },
  "SchoolStatisticsController.getStatistics": {
    "method": "GET",
    "path": "/api/reports/school-statistics"
  },
  "SchoolStatisticsController.exportStatisticsPdf": {
    "method": "GET",
    "path": "/api/reports/school-statistics/export/pdf"
  },
  "SchoolStatisticsController.exportExcel": {
    "method": "GET",
    "path": "/api/reports/school-statistics/export/excel"
  },
  "StudentInterestReportController.getReport": {
    "method": "GET",
    "path": "/api/reports/student-interests"
  },
  "StudentInterestReportController.exportStudentInterestPdf": {
    "method": "GET",
    "path": "/api/reports/student-interests/export/pdf"
  },
  "SubdistrictController.getAll": {
    "method": "GET",
    "path": "/api/subdistricts"
  },
  "SubdistrictController.getById": {
    "method": "GET",
    "path": "/api/subdistricts/{id}"
  },
  "SubdistrictController.create": {
    "method": "POST",
    "path": "/api/subdistricts"
  },
  "SubdistrictController.update": {
    "method": "PUT",
    "path": "/api/subdistricts/{id}"
  },
  "SubdistrictController.delete": {
    "method": "DELETE",
    "path": "/api/subdistricts/{id}"
  },
  "SystemOverviewReportController.getSystemOverview": {
    "method": "GET",
    "path": "/api/admin/reports/system-overview"
  },
  "SystemOverviewReportExportController.exportCsv": {
    "method": "GET",
    "path": "/api/admin/reports/system-overview/export/csv"
  },
  "SystemOverviewReportExportController.exportPdf": {
    "method": "GET",
    "path": "/api/admin/reports/system-overview/export/pdf"
  },
  "TeacherProgramInterestStatisticsController.getStatistics": {
    "method": "GET",
    "path": "/api/reports/program-interest-statistics"
  },
  "TeacherProgramInterestStatisticsController.exportStatisticsPdf": {
    "method": "GET",
    "path": "/api/reports/program-interest-statistics/export/pdf"
  }
};
  let token = null;
  let loginVersion = 0;
  let configuration = {baseUrl: window.location.origin, endpoints: {}, getHeaders: () => ({})};
  class ApiError extends Error {
    constructor(status, message, details) { super(message); this.name = 'ApiError'; this.status = status; this.details = details; }
  }
  function configure({baseUrl = window.location.origin, endpoints = {}, getHeaders = () => ({})} = {}) {
    configuration = {baseUrl, endpoints, getHeaders};
  }
  async function request(operation, options = {}) {
    const definition = configuration.endpoints[operation] || routes[operation];
    if (!definition) throw new Error('Unknown API operation: ' + operation);
    const route = typeof definition === 'string' ? {path: definition, method: 'GET'} : definition;
    let path = route.path.replace(/\{(\w+)\}/g, (_, key) => {
      if (options.params?.[key] == null) throw new Error('Missing path parameter: ' + key);
      return encodeURIComponent(String(options.params[key]));
    });
    const base = new URL(configuration.baseUrl, window.location.href);
    const url = new URL(path, base);
    // Do not leak Bearer credentials to an accidentally configured external host.
    if (url.origin !== base.origin) throw new Error('Endpoint origin must match API baseUrl');
    for (const [key, value] of Object.entries(options.query || {})) {
      if (value != null && value !== '') url.searchParams.set(key, String(value));
    }
    const form = options.body instanceof FormData;
    const headers = new Headers(configuration.getHeaders());
    if (token && options.auth !== false) headers.set('Authorization', 'Bearer ' + token);
    if (options.auth === false) headers.delete('Authorization');
    if (options.body !== undefined && !form) headers.set('Content-Type', 'application/json');
    if (form) headers.delete('Content-Type');
    const response = await fetch(url, {
      method: options.method || route.method, headers, signal: options.signal,
      credentials: 'same-origin',
      ...(options.body !== undefined ? {body: form ? options.body : JSON.stringify(options.body)} : {})
    });
    if (!response.ok) {
      const text = await response.text();
      let details = text; try { details = JSON.parse(text); } catch {}
      if (response.status === 401 && options.auth !== false) token = null;
      throw new ApiError(response.status,
        typeof details === 'object' && details ? details.message || details.error || 'API request failed (' + response.status + ')' : text || 'API request failed (' + response.status + ')', details);
    }
    if (response.status === 204) return null;
    if (options.responseType === 'blob') return response.blob();
    const text = await response.text();
    if (!text) return null;
    if ((response.headers.get('Content-Type') || '').includes('json')) return JSON.parse(text);
    return text;
  }
  function requireEmail(value) {
    const email = typeof value === 'string' ? value.trim() : '';
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
      throw new Error('กรุณากรอกอีเมลให้ถูกต้อง');
    }
    return email.toLowerCase();
  }
  async function requestPasswordReset(email) {
    return request('LoginController.forgotPassword', {
      method: 'POST', auth: false, body: {email: requireEmail(email)}
    });
  }
  async function login(email, password) {
    const version = ++loginVersion;
    token = null;
    email = requireEmail(email);
    // Authenticate using the email field expected by the current backend.
    const result = await request('LoginController.login', {auth:false, body:{email,password}});
    if (version !== loginVersion) throw new Error('Login request cancelled');
    if (typeof result?.token !== 'string' || !result.token) throw new Error('Login response has no token');
    token = result.token;
    return result;
}
  async function download(operation, options = {}, filename = 'report.pdf') {
    const blob = await request(operation, {...options, responseType:'blob'});
    const url = URL.createObjectURL(blob), a = document.createElement('a');
    a.href = url; a.download = filename; a.click(); setTimeout(() => URL.revokeObjectURL(url), 1000);
  }
  async function verifyEmail(verificationToken, type) {
  if (!verificationToken?.trim()) {
    throw new Error("กรุณาระบุ Token ยืนยันอีเมล");
  }
  return request("EmailVerificationController.verify", {
    method: "GET",
    auth: false,
    query: {
      token: verificationToken.trim(),
      ...(type ? { type } : {})
    }
  });
}
return { configure, request,
  login,
  requestPasswordReset,
  verifyEmail,
  download,
  logout() { loginVersion++; token = null; },
  ApiError,
  operations: Object.freeze(
    Object.fromEntries(
      Object.entries(routes).map(([k, v]) => [
        k,
        Object.freeze({ ...v })
      ])
    )
  )
};
})();
