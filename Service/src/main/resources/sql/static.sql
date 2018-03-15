 create table objectinfo(id char(32) not null primary key, \
                        person.name varchar, \
                        person.platformid varchar, \
                        person.tag varchar, \
                        person.pkey varchar, \
                        person.idcard varchar, \
                        person.sex integer, \
                        person.photo varbinary, \
                        person.feature float[], \
                        person.reason varchar, \
                        person.creator varchar, \
                        person.cphone varchar, \
                        person.createtime date, \
                        person.updatetime date, \
                        person.important integer, \
                        person.status integer);