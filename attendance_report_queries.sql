/* get student names and ids for those that are present in this period */
SELECT students.student_name, students._id  FROM students WHERE students._id in (
SELECT student_to_period.student_id         FROM student_to_period WHERE student_to_period.period_id IN (
SELECT periods._id                          FROM periods WHERE periods.term_id =
(SELECT terms._id                           FROM terms WHERE terms.start_date = 1530671431479)));
