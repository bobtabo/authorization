package schema

import (
	"entgo.io/ent"
	"entgo.io/ent/dialect/entsql"
	"entgo.io/ent/schema"
	"entgo.io/ent/schema/field"
	"time"
)

// Staff holds the schema definition for the Staff entity.
type Staff struct {
	ent.Schema
}

func (Staff) Annotations() []schema.Annotation {
	return []schema.Annotation{
		entsql.Annotation{Table: "staffs"},
	}
}

func (Staff) Fields() []ent.Field {
	return []ent.Field{
		field.Uint("id").Positive().Immutable(),
		field.String("name"),
		field.String("email"),
		field.Int("provider"),
		field.String("provider_id"),
		field.String("avatar").Optional().Nillable(),
		field.Int("role").Default(2),
		field.Time("last_login_at").Optional().Nillable(),
		field.Time("created_at").Default(time.Now),
		field.Uint("created_by").Optional().Nillable(),
		field.Time("updated_at").Default(time.Now),
		field.Uint("updated_by").Optional().Nillable(),
		field.Time("deleted_at").Optional().Nillable(),
		field.Uint("deleted_by").Optional().Nillable(),
		field.Int("version").Default(0),
	}
}

func (Staff) Edges() []ent.Edge {
	return nil
}
